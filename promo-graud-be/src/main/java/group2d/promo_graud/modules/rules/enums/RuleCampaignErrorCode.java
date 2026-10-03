package group2d.promo_graud.modules.rules.enums;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
public enum RuleCampaignErrorCode implements BaseErrorCode {
    RULE_NOT_EXIST(1501, "Rule campaign doesn't exist", HttpStatus.BAD_REQUEST),
    CAMPAIGN_NOT_FOUND(1502, "Campaign doesn't exist", HttpStatus.NOT_FOUND),

    // Validate Time
    INVALID_TIME_RANGE(1503, "End time must be strictly after start time", HttpStatus.BAD_REQUEST),
    START_TIME_IN_PAST(1504, "Start time must be in the present or future", HttpStatus.BAD_REQUEST),
    RULE_TIME_OUTSIDE_CAMPAIGN(
            1505,
            "Rule timeframe must be completely within the campaign's timeframe",
            HttpStatus.BAD_REQUEST),

    // Validate Logic Rule
    INVALID_PERCENTAGE_VALUE(
            1506,
            "Percentage discount value must be strictly between 0 and 100",
            HttpStatus.BAD_REQUEST),
    MISSING_MAX_DISCOUNT(
            1507,
            "Max discount value is required and must be > 0 for percentage rules",
            HttpStatus.BAD_REQUEST),
    INVALID_FIXED_AMOUNT(
            1508, "Fixed discount amount must be greater than 0", HttpStatus.BAD_REQUEST),
    MIN_ORDER_LESS_THAN_DISCOUNT(
            1509,
            "Minimum order value cannot be less than the discount amount",
            HttpStatus.BAD_REQUEST),
    MISSING_PAYLOAD(
            1510,
            "JSON payload configuration is required for TIERED or BUY_X_GET_Y rules",
            HttpStatus.BAD_REQUEST);
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    RuleCampaignErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
