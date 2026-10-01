package group2d.promo_graud.modules.products.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    @NotBlank(message = "product name is required")
    String name;

    String images;
    String description;

    @NotNull(message = "product price is required")
    @Positive(message = "price must be positive")
    BigDecimal price;

    @NotNull(message = "type of product is required")
    Integer typeOfProduct;
}
