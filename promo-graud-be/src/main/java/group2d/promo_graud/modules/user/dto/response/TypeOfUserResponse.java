package group2d.promo_graud.modules.user.dto.response;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.user.enums.UserTypeEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TypeOfUserResponse {
    Integer id;
    UserTypeEnum type;
    BigDecimal threshold;
}
