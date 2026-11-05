package group2d.promo_graud.modules.orders.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemRequest {
    Integer orderId;
    @NotNull Integer productId;

    @NotNull
    @Min(1)
    Integer quantity;

    BigDecimal price;
}
