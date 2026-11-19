package group2d.promo_graud.modules.vnpay;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import group2d.promo_graud.modules.orders.OrderPaymentEnum;
import group2d.promo_graud.modules.orders.OrderPaymentStatusEnum;
import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.orders.entity.Order;
import group2d.promo_graud.modules.orders.repository.OrderRepository;
import group2d.promo_graud.modules.orders.service.OrderService;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.repository.UserRepository;

@SpringBootTest
@TestPropertySource(
        properties = {
            "vnpay.tmn-code=TEST_TMN_CODE",
            "vnpay.hash-secret=test-hash-secret-for-integration-test",
            "vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
            "vnpay.return-url=http://localhost:8080/api/v1/payments/vnpay/return",
            "app.frontend-url=http://localhost:5173"
        })
class PaymentConcurrencyIntegrationTest {

    @Autowired private PaymentService paymentService;

    @Autowired private ExpiredOrderScheduler expiredOrderScheduler;

    @Autowired private OrderRepository orderRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private EntityManager entityManager;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    // Không thực hiện hoàn voucher thật trong test.
    @MockitoBean private OrderService orderService;

    /** TEST 1: Cronjob hủy đơn trước, IPN thành công đến sau. */
    @Test
    void shouldKeepOrderFailedWhenSuccessfulIpnArrivesLate() {

        // Arrange
        Order order = createPendingOrder();
        Integer orderId = order.getId();

        // Act 1: Cronjob xử lý đơn quá hạn trước.
        expiredOrderScheduler.handleExpiredVnpayOrders();

        entityManager.clear();

        Order afterCron = orderRepository.findById(orderId).orElseThrow();

        assertEquals(OrderStatus.FAILED, afterCron.getStatus());
        assertEquals(OrderPaymentStatusEnum.FAILED, afterCron.getPaymentStatus());

        // Act 2: IPN thanh toán thành công đến trễ.
        Map<String, String> ipnParams = createSuccessfulIpn(order);

        Map<String, String> response = paymentService.processIpn(ipnParams);

        entityManager.clear();

        Order finalOrder = orderRepository.findById(orderId).orElseThrow();

        // Assert
        assertEquals("00", response.get("RspCode"));

        assertEquals(OrderStatus.FAILED, finalOrder.getStatus());

        assertEquals(OrderPaymentStatusEnum.FAILED, finalOrder.getPaymentStatus());

        assertEquals("VN12345678", finalOrder.getTransactionNo());

        // Cronjob đã gọi xử lý hoàn voucher một lần.
        verify(orderService, times(1)).releaseVoucher(any(Order.class));
    }

    /** TEST 2: IPN thành công trước, Cronjob chạy sau. */
    @Test
    void shouldNotCancelOrderWhenIpnHasAlreadyPaid() {

        // Arrange
        Order order = createPendingOrder();
        Integer orderId = order.getId();
        System.out.println("==========================================");
        System.out.println(
                "[TEST 2] Khởi tạo Order ID = "
                        + orderId
                        + " | status = "
                        + order.getStatus()
                        + " | paymentStatus = "
                        + order.getPaymentStatus());

        // Act 1: IPN thanh toán thành công trước.
        Map<String, String> ipnParams = createSuccessfulIpn(order);

        Map<String, String> response = paymentService.processIpn(ipnParams);

        assertEquals("00", response.get("RspCode"));

        entityManager.clear();

        Order afterIpn = orderRepository.findById(orderId).orElseThrow();

        System.out.println(
                "[TEST 2] Sau khi IPN chạy -> RspCode = "
                        + response.get("RspCode")
                        + " | status = "
                        + afterIpn.getStatus()
                        + " | paymentStatus = "
                        + afterIpn.getPaymentStatus()
                        + " | txnNo = "
                        + afterIpn.getTransactionNo());

        assertEquals(OrderPaymentStatusEnum.PAID, afterIpn.getPaymentStatus());

        // Act 2: Cronjob chạy sau.
        expiredOrderScheduler.handleExpiredVnpayOrders();

        entityManager.clear();

        Order finalOrder = orderRepository.findById(orderId).orElseThrow();

        System.out.println(
                "[TEST 2] Sau khi Cronjob chạy -> status = "
                        + finalOrder.getStatus()
                        + " | paymentStatus = "
                        + finalOrder.getPaymentStatus()
                        + " | txnNo = "
                        + finalOrder.getTransactionNo());
        System.out.println("==========================================");

        // Assert
        assertEquals(OrderStatus.PENDING, finalOrder.getStatus());

        assertEquals(OrderPaymentStatusEnum.PAID, finalOrder.getPaymentStatus());

        assertEquals("VN12345678", finalOrder.getTransactionNo());

        // Đơn đã thanh toán nên không được hoàn voucher.
        verify(orderService, never()).releaseVoucher(any(Order.class));
    }

    /** TEST 3: IPN và Cronjob cùng cập nhật một đơn hàng. Chỉ một UPDATE được phép thành công. */
    @Test
    void shouldAllowOnlyOneTransitionWhenIpnAndCronjobRunConcurrently() throws Exception {

        // Arrange
        Order order = createPendingOrder();
        Integer orderId = order.getId();
        System.out.println("==========================================");
        System.out.println("[TEST 3] Bắt đầu race condition cho Order ID = " + orderId);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Thread 1: IPN muốn chuyển đơn sang PAID.
            Future<Integer> ipnFuture =
                    executor.submit(
                            () -> {
                                ready.countDown();
                                start.await();

                                int rows =
                                        orderRepository.transition(
                                                orderId,
                                                OrderStatus.PENDING,
                                                OrderPaymentStatusEnum.UNPAID,
                                                OrderStatus.PENDING,
                                                OrderPaymentStatusEnum.PAID,
                                                "VN12345678");
                                System.out.println(
                                        "[TEST 3 - Thread IPN] Thực thi xong -> updated = " + rows);
                                return rows;
                            });

            // Thread 2: Cronjob muốn chuyển đơn sang FAILED.
            Future<Integer> cronFuture =
                    executor.submit(
                            () -> {
                                ready.countDown();
                                start.await();

                                int rows =
                                        orderRepository.transitionStatus(
                                                orderId,
                                                OrderStatus.PENDING,
                                                OrderPaymentStatusEnum.UNPAID,
                                                OrderStatus.FAILED,
                                                OrderPaymentStatusEnum.FAILED);
                                System.out.println(
                                        "[TEST 3 - Thread Cron] Thực thi xong -> updated = "
                                                + rows);
                                return rows;
                            });

            // Chờ cả hai thread sẵn sàng.
            ready.await();

            // Cho hai thread bắt đầu gần như đồng thời.
            start.countDown();

            int ipnUpdated = ipnFuture.get();
            int cronUpdated = cronFuture.get();

            System.out.println(
                    "[TEST 3] Kết quả cập nhật DB: ipnUpdated = "
                            + ipnUpdated
                            + ", cronUpdated = "
                            + cronUpdated);

            // Chỉ một trong hai UPDATE được thành công.
            assertEquals(1, ipnUpdated + cronUpdated);

            assertTrue(
                    (ipnUpdated == 1 && cronUpdated == 0) || (ipnUpdated == 0 && cronUpdated == 1));

            entityManager.clear();

            Order finalOrder = orderRepository.findById(orderId).orElseThrow();

            // Kiểm tra trạng thái cuối cùng.
            if (ipnUpdated == 1) {
                System.out.println(
                        "[TEST 3] => IPN THẮNG! Trạng thái cuối: status = "
                                + finalOrder.getStatus()
                                + " | paymentStatus = "
                                + finalOrder.getPaymentStatus()
                                + " | txnNo = "
                                + finalOrder.getTransactionNo());

                assertEquals(OrderStatus.PENDING, finalOrder.getStatus());

                assertEquals(OrderPaymentStatusEnum.PAID, finalOrder.getPaymentStatus());

                assertEquals("VN12345678", finalOrder.getTransactionNo());

            } else {
                System.out.println(
                        "[TEST 3] => CRONJOB THẮNG! Trạng thái cuối: status = "
                                + finalOrder.getStatus()
                                + " | paymentStatus = "
                                + finalOrder.getPaymentStatus()
                                + " | txnNo = "
                                + finalOrder.getTransactionNo());

                assertEquals(OrderStatus.FAILED, finalOrder.getStatus());

                assertEquals(OrderPaymentStatusEnum.FAILED, finalOrder.getPaymentStatus());
            }
            System.out.println("==========================================");

        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Tạo đơn hàng VNPAY PENDING + UNPAID. createdAt được chỉnh thành 25 phút trước để Cronjob tìm
     * thấy.
     */
    private Order createPendingOrder() {

        User user =
                userRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Database test chưa có User. Hãy seed một user trước."));

        Order order =
                Order.builder()
                        .user(user)
                        .totalPrice(new BigDecimal("100000"))
                        .finalPrice(new BigDecimal("100000"))
                        .paymentMethod(OrderPaymentEnum.VNP)
                        .status(OrderStatus.PENDING)
                        .paymentStatus(OrderPaymentStatusEnum.UNPAID)
                        .transactionNo(null)
                        .isDeleted(false)
                        .build();

        order = orderRepository.saveAndFlush(order);

        // Giả lập đơn đã tạo cách đây 25 phút.
        LocalDateTime oldTime = LocalDateTime.now().minusMinutes(25);

        jdbcTemplate.update(
                """
      UPDATE "order"
      SET created_at = ?
      WHERE id = ?
      """,
                Timestamp.valueOf(oldTime),
                order.getId());

        return order;
    }

    /** Tạo IPN thành công có chữ ký hợp lệ. */
    private Map<String, String> createSuccessfulIpn(Order order) {

        Map<String, String> params = new HashMap<>();

        params.put("vnp_TxnRef", String.valueOf(order.getId()));

        long amount = order.getFinalPrice().multiply(BigDecimal.valueOf(100)).longValueExact();

        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TransactionNo", "VN12345678");
        params.put("vnp_TmnCode", "TESTCODE");
        params.put("vnp_CurrCode", "VND");

        String hashData = buildHashData(params);

        String secureHash = VNPayUtils.hmacSHA512(hashSecret, hashData);

        params.put("vnp_SecureHash", secureHash);

        return params;
    }

    /**
     * Tạo chuỗi hash theo thứ tự key tăng dần. Phải đồng nhất với logic xác thực trong
     * PaymentService.
     */
    private String buildHashData(Map<String, String> params) {

        List<String> fieldNames = new ArrayList<>(params.keySet());

        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName)
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII))
                        .append("&");
            }
        }

        if (!hashData.isEmpty()) {
            hashData.setLength(hashData.length() - 1);
        }

        return hashData.toString();
    }
}
