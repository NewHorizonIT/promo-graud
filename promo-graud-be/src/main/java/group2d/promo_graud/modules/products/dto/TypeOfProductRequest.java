package group2d.promo_graud.modules.products.dto;

import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TypeOfProductRequest {
    @Size(min = 2, max = 100, message = "type name must more than 2 word")
    String type;
}
