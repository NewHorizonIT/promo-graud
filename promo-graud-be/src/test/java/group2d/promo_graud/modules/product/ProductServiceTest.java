package group2d.promo_graud.modules.product;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import group2d.promo_graud.modules.products.ProductErrorCode;
import group2d.promo_graud.modules.products.ProductService;
import group2d.promo_graud.modules.products.dto.ProductRequest;
import group2d.promo_graud.modules.products.dto.ProductResponse;
import group2d.promo_graud.modules.products.entity.Product;
import group2d.promo_graud.modules.products.entity.TypeOfProduct;
import group2d.promo_graud.modules.products.repository.ProductRepository;
import group2d.promo_graud.modules.products.repository.TypeOfProductRepository;
import group2d.promo_graud.shared.exception.AppException;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock TypeOfProductRepository typeOfProductRepository;
    @InjectMocks ProductService productService;

    @Test
    void getProductByIdSuccess() {
        when(productRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(mockProduct()));

        ProductResponse result = productService.getProductById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("iPhone", result.getName());
        assertEquals("Apple", result.getDescription());
        assertEquals(BigDecimal.valueOf(20000), result.getPrice());
    }

    @Test
    void getProductByIdNotFound() {
        when(productRepository.findByIdAndIsDeletedFalse(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> productService.getProductById(99));
        assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void createProductSuccess() {
        when(typeOfProductRepository.findById(1)).thenReturn(Optional.of(mockType()));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct());

        ProductResponse result = productService.createProduct(mockRequest());

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("iPhone", result.getName());
        assertEquals(1, result.getTypeId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProductTypeNotFound() {
        when(typeOfProductRepository.findById(99)).thenReturn(Optional.empty());

        ProductRequest request =
                ProductRequest.builder()
                        .name("iPhone")
                        .description("Apple")
                        .images("https://example.com/iphone.jpg")
                        .price(BigDecimal.valueOf(20000))
                        .typeOfProduct(99)
                        .build();
        AppException ex =
                assertThrows(AppException.class, () -> productService.createProduct(request));
        assertEquals(ProductErrorCode.PRODUCT_TYPE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void updateProductSuccess() {
        when(productRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(mockProduct()));
        when(typeOfProductRepository.findById(1)).thenReturn(Optional.of(mockType()));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct());

        ProductResponse result = productService.updateProduct(1, mockRequest());

        assertNotNull(result);
        assertEquals("iPhone", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProductProductNotFound() {
        when(productRepository.findByIdAndIsDeletedFalse(99)).thenReturn(Optional.empty());

        AppException ex =
                assertThrows(
                        AppException.class, () -> productService.updateProduct(99, mockRequest()));
        assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void updateProductTypeNotFound() {
        when(productRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(mockProduct()));
        when(typeOfProductRepository.findById(99)).thenReturn(Optional.empty());

        ProductRequest request =
                ProductRequest.builder()
                        .name("iPhone")
                        .description("Apple")
                        .images("https://example.com/iphone.jpg")
                        .price(BigDecimal.valueOf(20000))
                        .typeOfProduct(99)
                        .build();
        AppException ex =
                assertThrows(AppException.class, () -> productService.updateProduct(1, request));
        assertEquals(ProductErrorCode.PRODUCT_TYPE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void deleteProductSuccess() {
        Product product = mockProduct();
        when(productRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(product));

        productService.deleteProduct(1);

        assertEquals(true, product.getIsDeleted());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProductNotFound() {
        when(productRepository.findByIdAndIsDeletedFalse(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> productService.deleteProduct(99));
        assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }

    private TypeOfProduct mockType() {
        return TypeOfProduct.builder().id(1).type("Electric").build();
    }

    private Product mockProduct() {
        return Product.builder()
                .id(1)
                .name("iPhone")
                .description("Apple")
                .price(BigDecimal.valueOf(20000))
                .typeOfProduct(mockType())
                .build();
    }

    private ProductRequest mockRequest() {
        return ProductRequest.builder()
                .name("iPhone")
                .description("Apple")
                .images("https://example.com/iphone.jpg")
                .price(BigDecimal.valueOf(20000))
                .typeOfProduct(1)
                .build();
    }
}
