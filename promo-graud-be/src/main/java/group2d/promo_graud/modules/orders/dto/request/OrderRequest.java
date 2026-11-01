package group2d.promo_graud.modules.orders.dto.request;

import java.util.List;

import jakarta.validation.Valid;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    Integer voucherId;
    @Valid List<OrderItemRequest> items;
}
