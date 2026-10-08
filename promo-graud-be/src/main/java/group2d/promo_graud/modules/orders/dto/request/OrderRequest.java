package group2d.promo_graud.modules.orders.dto.request;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.orders.OrderStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    Integer voucherId;
    OrderStatus status;
    List<OrderItemRequest> orderItemRequests;
}
