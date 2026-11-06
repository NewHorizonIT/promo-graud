package group2d.promo_graud.modules.fraud;

import group2d.promo_graud.shared.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum FraudErrorCode implements BaseErrorCode {
  USER_REUSED(1901, "User has already redeemed this voucher", HttpStatus.CONFLICT),
  DEVICE_REUSED(1902, "This device has already redeemed this voucher", HttpStatus.FORBIDDEN),
  MULTI_ACCOUNT_DEVICE(1903, "Too many accounts detected on this device", HttpStatus.FORBIDDEN),
  IP_LIMIT_EXCEEDED(1904, "Too many redemptions from this IP address", HttpStatus.TOO_MANY_REQUESTS),
  VISITOR_ID_REQUIRED(1905, "Device identifier is required", HttpStatus.BAD_REQUEST);

  private final int code;
  private final String message;
  private final HttpStatus statusCode;

  public static FraudErrorCode from(FraudReason reason) {
    return switch (reason) {
      case USER_REUSED -> USER_REUSED;
      case DEVICE_REUSED -> DEVICE_REUSED;
      case MULTI_ACCOUNT_DEVICE -> MULTI_ACCOUNT_DEVICE;
      case IP_LIMIT_EXCEEDED -> IP_LIMIT_EXCEEDED;
    };
  }


}
