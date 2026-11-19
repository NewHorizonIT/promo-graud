package group2d.promo_graud.modules.orders.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.orders.OrderPaymentEnum;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    Integer voucherId;
    @Valid List<OrderItemRequest> items;

    @NotNull(message = "Payment method is required")
    OrderPaymentEnum paymentMethod;
}
