package group2d.promo_graud.modules.user.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.user.enums.UserTypeEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TypeOfUserRequest {
    @NotNull(message = "Loại user không được để trống")
    UserTypeEnum type;

    @NotNull(message = "Ngưỡng để nâng cấp không được để trống")
    BigDecimal threshold;
}
