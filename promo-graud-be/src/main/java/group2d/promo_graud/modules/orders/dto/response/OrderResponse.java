package group2d.promo_graud.modules.orders.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.voucher.dto.responses.VoucherResponse;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Integer id;
    UserSummaryResponse user;
    BigDecimal totalPrice;
    BigDecimal finalPrice;
    VoucherResponse voucher;
    OrderStatus status;
    LocalDateTime createdAt;
    List<OrderItemResponse> items;
}
