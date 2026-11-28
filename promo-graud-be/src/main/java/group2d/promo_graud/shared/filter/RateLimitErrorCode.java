package group2d.promo_graud.shared.filter;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum RateLimitErrorCode implements BaseErrorCode {
    RATE_LIMIT_EXCEEDED(1950, "Too many requests, please slow down", HttpStatus.TOO_MANY_REQUESTS);

    private final int code;
    private final String message;
    private final HttpStatus statusCode;
}
