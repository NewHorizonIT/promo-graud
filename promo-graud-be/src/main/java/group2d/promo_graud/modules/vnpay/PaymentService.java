package group2d.promo_graud.modules.vnpay;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.orders.OrderErrorCode;
import group2d.promo_graud.modules.orders.OrderPaymentEnum;
import group2d.promo_graud.modules.orders.OrderPaymentStatusEnum;
import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.orders.entity.Order;
import group2d.promo_graud.modules.orders.repository.OrderRepository;
import group2d.promo_graud.shared.exception.AppException;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderRepository orderRepository;

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    public String createPaymentUrl(Integer orderId, String ipAddress) {
        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!order.getUser().getUsername().equals(currentUser)) {
            throw new AppException(OrderErrorCode.ORDER_NOT_EXISTS);
        }
        if (order.getPaymentMethod() != OrderPaymentEnum.VNP
                || order.getStatus() != OrderStatus.PENDING
                || order.getPaymentStatus() != OrderPaymentStatusEnum.UNPAID) {
            throw new AppException(OrderErrorCode.ORDER_STATUS);
        }

        long amount = order.getFinalPrice().longValue() * 100;

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", String.valueOf(orderId));
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress != null ? ipAddress : "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnpParams.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15);
        vnpParams.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName)
                            .append("=")
                            .append(
                                    URLEncoder.encode(
                                            fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                            .append("=")
                            .append(
                                    URLEncoder.encode(
                                            fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append("&");
                    hashData.append("&");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error encoding VNPay params", e);
        }

        query.setLength(query.length() - 1);
        hashData.setLength(hashData.length() - 1);

        String vnpSecureHash = VNPayUtils.hmacSHA512(hashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnpSecureHash);

        return vnpayUrl + "?" + query.toString();
    }

    public Map<String, String> processIpn(Map<String, String> queryParams) {
        String vnpSecureHash = queryParams.remove("vnp_SecureHash");
        queryParams.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(queryParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        try {
            for (String fieldName : fieldNames) {
                String fieldValue = queryParams.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName)
                            .append("=")
                            .append(
                                    URLEncoder.encode(
                                            fieldValue, StandardCharsets.US_ASCII.toString()))
                            .append("&");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        hashData.setLength(hashData.length() - 1);

        String signValue = VNPayUtils.hmacSHA512(hashSecret, hashData.toString());
        if (!signValue.equals(vnpSecureHash)) {
            return Map.of("RspCode", "97", "Message", "Invalid Checksum");
        }

        // LƯU Ý: Vẫn đang cố tình bỏ qua check Idempotency (order.getStatus() == PENDING)
        // Để bạn có thể test gọi IPN nhiều lần sinh lỗi như yêu cầu.
        Integer orderId;
        try {
            orderId = Integer.parseInt(queryParams.get("vnp_TxnRef").split("_")[0]);
        } catch (Exception e) {
            return Map.of("RspCode", "01", "Message", "Order not found");
        }
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return Map.of("RspCode", "01", "Message", "Order not found");
        }

        long vnpAmount = Long.parseLong(queryParams.get("vnp_Amount"));
        if (vnpAmount != order.getFinalPrice().longValue() * 100) {
            return Map.of("RspCode", "04", "Message", "Invalid amount");
        }

        if (!"00".equals(queryParams.get("vnp_ResponseCode"))) {
            // Khách hủy / thẻ lỗi: giữ PENDING + UNPAID để thanh toán lại
            return Map.of("RspCode", "00", "Message", "Confirm Success");
        }

        int updated =
                orderRepository.transition(
                        orderId,
                        OrderStatus.PENDING,
                        OrderPaymentStatusEnum.UNPAID,
                        OrderStatus.PENDING,
                        OrderPaymentStatusEnum.PAID,
                        queryParams.get("vnp_TransactionNo"));
        if (updated == 0) {
            return Map.of("RspCode", "02", "Message", "Order already confirmed");
        } else {
            return Map.of("RspCode", "00", "Message", "Confirm Success");
        }
    }
}
