package group2d.promo_graud.modules.voucher.enums;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum DistributionErrorCode implements BaseErrorCode {
    DISTRIBUTION_NOT_FOUND(1901, "distributionLog not found", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
