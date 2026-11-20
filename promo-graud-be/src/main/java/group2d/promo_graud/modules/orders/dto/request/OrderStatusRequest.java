package group2d.promo_graud.modules.orders.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.orders.OrderStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatusRequest {
    @NotNull OrderStatus status;
}
