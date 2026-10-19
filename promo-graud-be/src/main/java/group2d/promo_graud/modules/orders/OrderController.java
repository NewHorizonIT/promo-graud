package group2d.promo_graud.modules.orders;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.orders.dto.request.OrderRequest;
import group2d.promo_graud.modules.orders.dto.response.OrderResponse;
import group2d.promo_graud.modules.orders.service.OrderService;
import group2d.promo_graud.shared.dto.ApiResponse;
import group2d.promo_graud.shared.dto.PaginatedResponse;

@RestController
@RequestMapping("/api/v1/orders")
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

    @GetMapping("/filter-by-time")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> filterByTime(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<OrderResponse> orders = orderService.filterByTime(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .code(200)
                        .message("Filter orders by time success")
                        .result(orders)
                        .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PaginatedResponse<OrderResponse>>> getMyOrder(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Integer userId = ((Number) jwt.getClaim("userId")).intValue();

        Page<OrderResponse> orders = orderService.findByUserId(userId, page, limit);

        PaginatedResponse<OrderResponse> result =
                PaginatedResponse.<OrderResponse>builder()
                        .data(orders.getContent())
                        .page(page)
                        .size(limit)
                        .totalItems(orders.getTotalElements())
                        .totalPages(orders.getTotalPages())
                        .build();

        return ResponseEntity.ok(
                ApiResponse.<PaginatedResponse<OrderResponse>>builder()
                        .code(200)
                        .message("Lấy lịch sử đơn hàng thành công")
                        .result(result)
                        .build());
    }
}
