package group2d.promo_graud.modules.user.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
    USER_EXISTS(1600, "Tên đăng nhập đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTS(1601, "User not exists", HttpStatus.BAD_REQUEST),
    USER_EMAIL_EXISTS(1602, "Email đã được sử dụng", HttpStatus.BAD_REQUEST);
    int code;
    String message;
    HttpStatusCode statusCode;
}
