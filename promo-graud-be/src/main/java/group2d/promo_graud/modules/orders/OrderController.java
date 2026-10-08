package group2d.promo_graud.modules.orders;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.orders.dto.request.OrderRequest;
import group2d.promo_graud.modules.orders.dto.response.OrderResponse;
import group2d.promo_graud.modules.orders.service.OrderService;
import group2d.promo_graud.shared.dto.ApiResponse;

@Controller("/api/v1/oder")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestBody OrderRequest request) {
        OrderResponse orderResponse = orderService.createOrder(request);
        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .code(201)
                        .message("Create order success")
                        .result(orderResponse)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> findAll() {
        List<OrderResponse> listOrder = orderService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .code(200)
                        .message("Find all orders success")
                        .result(listOrder)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> findById(@PathVariable Integer id) {
        OrderResponse orderResponse = orderService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .code(200)
                        .message("Find by order id success")
                        .result(orderResponse)
                        .build());
    }
}
