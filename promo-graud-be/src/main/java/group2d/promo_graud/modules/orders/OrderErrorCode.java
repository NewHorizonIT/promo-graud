package group2d.promo_graud.modules.orders;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum OrderErrorCode implements BaseErrorCode {
    ORDER_NOT_EXISTS(1300, "Order not exists", HttpStatus.BAD_REQUEST);
    int code;
    String message;
    HttpStatusCode statusCode;
}
