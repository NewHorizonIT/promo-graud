package group2d.promo_graud.modules.user.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public enum TypeOfUserErrorCode implements BaseErrorCode {
    TYPE_OF_USER_EXISTS(1600, "Type of user đã tồn tại", HttpStatus.BAD_REQUEST),
    TYPE_OF_USER_NOT_EXISTS(1601, "Không tìm thấy loại user", HttpStatus.BAD_REQUEST);
    int code;
    String message;
    HttpStatusCode statusCode;
}
