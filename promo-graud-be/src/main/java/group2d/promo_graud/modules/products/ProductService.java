package group2d.promo_graud.modules.products;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.products.dto.ProductRequest;
import group2d.promo_graud.modules.products.dto.ProductResponse;
import group2d.promo_graud.modules.products.dto.TypeOfProductRequest;
import group2d.promo_graud.modules.products.dto.TypeOfProductResponse;
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

    public Page<ProductResponse> getProducts(
            Integer typeId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String name,
            int size,
            int page) {
        Pageable pageable = PageRequest.of(page - 1, size);
        String keyword = name == null ? "" : name.trim();
        return productRepository
                .getAll(typeId, minPrice, maxPrice, keyword, pageable)
                .map(this::toResponse);
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

    public TypeOfProductResponse createType(TypeOfProductRequest request) {
        TypeOfProduct typeOfProduct = TypeOfProduct.builder().type(request.getType()).build();

        return toTypeResponse(typeOfProductRepository.save(typeOfProduct));
    }

    public TypeOfProductResponse updateType(Integer id, TypeOfProductRequest request) {
        TypeOfProduct type =
                typeOfProductRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new AppException(ProductErrorCode.PRODUCT_TYPE_NOT_FOUND));
        type.setType(request.getType());
        return toTypeResponse(typeOfProductRepository.save(type));
    }

    public Page<TypeOfProductResponse> getTypeProducts(int size, int page) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return typeOfProductRepository.findAll(pageable).map(this::toTypeResponse);
    }

    // map cua product entity
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

    // map cua type of product entity
    private TypeOfProductResponse toTypeResponse(TypeOfProduct typeOfProduct) {
        return TypeOfProductResponse.builder()
                .id(typeOfProduct.getId())
                .type(typeOfProduct.getType())
                .build();
    }

    // Get type of product
    public List<TypeOfProduct> getTypes() {
        return typeOfProductRepository.findAll(Sort.by("id"));
    }
}
