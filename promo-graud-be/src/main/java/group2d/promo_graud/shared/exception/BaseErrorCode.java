package group2d.promo_graud.shared.exception;

import org.springframework.http.HttpStatusCode;

public interface BaseErrorCode {
    int getCode();

    String getMessage();

    HttpStatusCode getStatusCode();
}
