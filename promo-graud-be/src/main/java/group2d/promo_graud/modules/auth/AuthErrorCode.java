package group2d.promo_graud.modules.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
public enum AuthErrorCode implements BaseErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1004, "Request isn't authenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1005, "No access", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1006, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1007, "Token đã hết hạn", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALIDATED(1008, "Token đã bị vô hiệu hóa ", HttpStatus.UNAUTHORIZED);
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    AuthErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
