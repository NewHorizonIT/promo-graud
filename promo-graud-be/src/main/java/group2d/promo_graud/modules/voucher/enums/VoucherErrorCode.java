package group2d.promo_graud.modules.voucher.enums;

import group2d.promo_graud.shared.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum VoucherErrorCode implements BaseErrorCode {
  DUPLICATE_CODE(1801, "Code is duplicate", HttpStatus.BAD_REQUEST),
  INSUFFICIENT_QUANTITY(1802,"Vouchers have been created but the required number is not yet available",HttpStatus.MULTI_STATUS),
  REQUIRED_FIELD(1803,"Minimum one status or quantity_remain field ",HttpStatus.BAD_REQUEST),
  NOT_EXIST(1804,"Voucher does not exist",HttpStatus.NOT_FOUND),
  GENERIC_QUANTITY_NOT_EDITABLE(1806, "Quantity of a UNIQUE voucher cannot be modified", HttpStatus.UNPROCESSABLE_ENTITY),
  INVALID_STATUS(1807, "Invalid voucher status", HttpStatus.BAD_REQUEST),;
  private final int code;
  private final String message;
  private final HttpStatusCode statusCode;

  VoucherErrorCode(int code, String message, HttpStatusCode statusCode) {
    this.code = code;
    this.message = message;
    this.statusCode = statusCode;
  }
}
