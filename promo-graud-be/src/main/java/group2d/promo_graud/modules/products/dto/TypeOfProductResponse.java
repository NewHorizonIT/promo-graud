package group2d.promo_graud.modules.products.dto;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TypeOfProductResponse {
    Integer id;
    String type;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
