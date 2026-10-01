package group2d.promo_graud.modules.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.products.dto.ProductRequest;
import group2d.promo_graud.modules.products.dto.ProductResponse;
import group2d.promo_graud.modules.products.entity.Product;
import group2d.promo_graud.modules.products.entity.TypeOfProduct;
import group2d.promo_graud.modules.products.repository.ProductRepository;
import group2d.promo_graud.modules.products.repository.TypeOfProductRepository;
import group2d.promo_graud.shared.exception.AppException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {

    ProductRepository productRepository;
    TypeOfProductRepository typeOfProductRepository;

    public Page<ProductResponse> getProducts(Integer typeId, int limit, int offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Product> page =
                typeId != null
                        ? productRepository.findAllByTypeOfProductId(typeId, pageable)
                        : productRepository.findAllByIsDeletedFalse(pageable);
        return page.map(this::toResponse);
    }

    public ProductResponse getProductById(Integer id) {
        Product product =
                productRepository
                        .findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));
        return toResponse(product);
    }

    public ProductResponse createProduct(ProductRequest request) {
        TypeOfProduct type =
                typeOfProductRepository
                        .findById(request.getTypeOfProduct())
                        .orElseThrow(
                                () -> new AppException(ProductErrorCode.PRODUCT_TYPE_NOT_FOUND));
        Product product =
                Product.builder()
                        .name(request.getName())
                        .images(request.getImages())
                        .description(request.getDescription())
                        .price(request.getPrice())
                        .typeOfProduct(type)
                        .build();
        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        Product product =
                productRepository
                        .findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));
        TypeOfProduct type =
                typeOfProductRepository
                        .findById(request.getTypeOfProduct())
                        .orElseThrow(
                                () -> new AppException(ProductErrorCode.PRODUCT_TYPE_NOT_FOUND));
        product.setName(request.getName());
        product.setImages(request.getImages());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setTypeOfProduct(type);
        return toResponse(productRepository.save(product));
    }

    public void deleteProduct(Integer id) {
        Product product =
                productRepository
                        .findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));
        product.setIsDeleted(true);
        productRepository.save(product);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .images(product.getImages())
                .description(product.getDescription())
                .price(product.getPrice())
                .typeId(
                        product.getTypeOfProduct() != null
                                ? product.getTypeOfProduct().getId()
                                : null)
                .typeName(
                        product.getTypeOfProduct() != null
                                ? product.getTypeOfProduct().getType()
                                : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
