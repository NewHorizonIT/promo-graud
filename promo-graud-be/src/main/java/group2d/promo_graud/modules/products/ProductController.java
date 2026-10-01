package group2d.promo_graud.modules.products;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.products.dto.PageResponse;
import group2d.promo_graud.modules.products.dto.ProductRequest;
import group2d.promo_graud.modules.products.dto.ProductResponse;
import group2d.promo_graud.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

    ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) Integer typeId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        Page<ProductResponse> pages = productService.getProducts(typeId, limit, offset);
        PageResponse<ProductResponse> result =
                PageResponse.<ProductResponse>builder()
                        .data(pages.getContent())
                        .page(pages.getNumber() + 1)
                        .total(pages.getTotalElements())
                        .totalPage(pages.getTotalPages())
                        .build();

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<ProductResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách sản phẩm thành công")
                        .result(result)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .code(200)
                        .result(productService.getProductById(id))
                        .message("Lấy chi tiết sản phẩm thành công")
                        .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestBody @Valid ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<ProductResponse>builder()
                                .code(201)
                                .result(productService.createProduct(request))
                                .message("Tạo sản phẩm thành công")
                                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Integer id, @RequestBody @Valid ProductRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .code(200)
                        .result(productService.updateProduct(id, request))
                        .message("Cập nhật sản phẩm thành công")
                        .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder().code(200).message("Xóa sản phẩm thành công").build());
    }
}
