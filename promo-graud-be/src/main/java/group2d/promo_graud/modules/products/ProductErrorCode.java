package group2d.promo_graud.modules.products;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
public enum ProductErrorCode implements BaseErrorCode {
    PRODUCT_NOT_FOUND(2001, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_TYPE_NOT_FOUND(2002, "Product type not found", HttpStatus.NOT_FOUND),
    PRODUCT_TYPE_HAS_EXISTED(2003, "Product type has existed", HttpStatus.BAD_REQUEST),
    PRODUCT_TYPE_IN_USE(2004, "Product type is in use by existing products", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ProductErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
