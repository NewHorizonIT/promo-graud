package group2d.promo_graud.modules.orders.dto.request;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemRequest {
    Integer orderId;
    Integer productId;
    Integer quantity;
    BigDecimal price;
}
