package group2d.promo_graud.modules.user.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.user.enums.RoleEnum;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    Integer id;
    String username;
    String email;
    RoleEnum role;
    Integer typeOfUserId;
    UserTypeEnum typeOfUserType;
    BigDecimal typeOfUserThreshold;
    Boolean isDelete;
    LocalDateTime createdAt;
    LocalDateTime updateAt;
    BigDecimal totalSpending;
}
