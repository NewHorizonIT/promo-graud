package group2d.promo_graud.modules.orders.dto.response;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.voucher.Voucher;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Integer id;
    User user;
    BigDecimal totalPrice;
    BigDecimal finalPrice;
    Voucher voucher;
    OrderStatus status;
}
