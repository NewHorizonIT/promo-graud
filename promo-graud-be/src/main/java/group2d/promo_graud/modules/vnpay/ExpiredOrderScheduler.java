package group2d.promo_graud.modules.vnpay;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.orders.OrderPaymentEnum;
import group2d.promo_graud.modules.orders.OrderPaymentStatusEnum;
import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.orders.entity.Order;
import group2d.promo_graud.modules.orders.repository.OrderRepository;
import group2d.promo_graud.modules.orders.service.OrderService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredOrderScheduler {
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(cron = "0 */5 * * * *")
    public void handleExpiredVnpayOrders() {
        LocalDateTime expireThreshold = LocalDateTime.now().minusMinutes(20);

        List<Order> expiredOrders =
                orderRepository.findByPaymentMethodAndStatusAndPaymentStatusAndCreatedAtBefore(
                        OrderPaymentEnum.VNP,
                        OrderStatus.PENDING,
                        OrderPaymentStatusEnum.UNPAID,
                        expireThreshold);

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info(
                "Tìm thấy {} đơn hàng VNPay quá hạn 15 phút. Tiến hành hủy và hoàn voucher...",
                expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                int updated =
                        orderRepository.transitionStatus(
                                order.getId(),
                                OrderStatus.PENDING,
                                OrderPaymentStatusEnum.UNPAID,
                                OrderStatus.FAILED,
                                OrderPaymentStatusEnum.FAILED);

                if (updated == 0) {
                    log.info(
                            "Đơn hàng ID {} đã thay đổi trạng thái (có thể vừa thanh toán), bỏ qua hủy.",
                            order.getId());
                    continue;
                }

                // 2. Gọi hàm hoàn voucher (Đã có sẵn Redis Lock & Transaction riêng bên trong hàm)
                orderService.releaseVoucher(order);

            } catch (Exception e) {
                log.error("Lỗi khi xử lý hủy đơn hàng ID {}: {}", order.getId(), e.getMessage());
            }
        }
    }
}
