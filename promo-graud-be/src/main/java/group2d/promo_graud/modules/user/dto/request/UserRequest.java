package group2d.promo_graud.modules.user.dto.request;

import jakarta.validation.constraints.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.user.enums.RoleEnum;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, message = "Password phải có ít nhất 6 kí tự")
    String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có ít nhất 6 k tự")
    String password;

    RoleEnum role;

    @NotNull(message = "TypeOfUser không được để trống")
    Integer typeOfUser;
}
