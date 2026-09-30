package group2d.promo_graud.modules.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
    @Size(min = 3, message = "Password phải có ít nhất 6 k tự")
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
