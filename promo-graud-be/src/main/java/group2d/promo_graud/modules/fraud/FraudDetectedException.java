package group2d.promo_graud.modules.fraud;

import lombok.Getter;

/** Exception nội bộ: ném trong transaction để rollback, order sẽ bắt rồi đổi sang AppException. */
@Getter
public class FraudDetectedException extends RuntimeException {

  private final FraudReason reason;

  public FraudDetectedException(FraudReason reason) {
    super(reason.name(), null, false, false); // tắt stack trace cho rẻ khi bị tấn công
    this.reason = reason;
  }
}
