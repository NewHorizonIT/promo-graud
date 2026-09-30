package group2d.promo_graud.modules.user.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.user.entity.TypeOfUser;
import group2d.promo_graud.modules.user.enums.RoleEnum;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    Integer id;
    String username;
    String email;
    String password;
    RoleEnum role;
    TypeOfUser typeOfUser;
    Boolean isDelete;
    LocalDateTime createdAt;
    LocalDateTime updateAt;
}
