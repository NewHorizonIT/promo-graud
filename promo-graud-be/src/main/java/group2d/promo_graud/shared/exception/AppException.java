package group2d.promo_graud.shared.exception;


//Custom Runtime Exception
public class AppException extends RuntimeException{
  private final BaseErrorCode errorCode;

  public AppException(BaseErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public BaseErrorCode getErrorCode() {
    return errorCode;
  }
}
