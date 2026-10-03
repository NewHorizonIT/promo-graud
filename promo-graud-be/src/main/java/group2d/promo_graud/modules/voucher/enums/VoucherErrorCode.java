package group2d.promo_graud.modules.voucher.enums;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
public enum VoucherErrorCode implements BaseErrorCode {
    DUPLICATE_CODE(1801, "Code is duplicate", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_QUANTITY(
            1802,
            "Vouchers have been created but the required number is not yet available",
            HttpStatus.MULTI_STATUS),
    REQUIRED_FIELD(1803, "Minimum one status or quantity_remain field ", HttpStatus.BAD_REQUEST),
    NOT_EXIST(1804, "Voucher does not exist", HttpStatus.NOT_FOUND),
    GENERIC_QUANTITY_NOT_EDITABLE(
            1806,
            "Quantity of a UNIQUE voucher cannot be modified",
            HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_STATUS(1807, "Invalid voucher status", HttpStatus.BAD_REQUEST),
  // ---------------- Batch generate voucher ----------------
  BATCH_TRIGGER_FAILED(
    1808,
    "Failed to start voucher generation job",
    HttpStatus.INTERNAL_SERVER_ERROR),
  BATCH_JOB_NOT_FOUND(1809, "Voucher generation job does not exist", HttpStatus.NOT_FOUND),
  BATCH_JOB_ALREADY_RUNNING(
    1810,
    "A voucher generation job with the same request is already running or completed",
    HttpStatus.CONFLICT),
  BATCH_JOB_FAILED(
    1811,
    "Voucher generation job failed",
    HttpStatus.INTERNAL_SERVER_ERROR),
  BATCH_JOB_STILL_PROCESSING(
    1812,
    "Voucher generation job is still processing",
    HttpStatus.ACCEPTED),
  BATCH_QUANTITY_EXCEEDED(
    1813,
    "Requested quantity exceeds the maximum allowed per job",
    HttpStatus.BAD_REQUEST),
  BATCH_TOO_MANY_JOBS(
    1814,
    "Too many voucher generation jobs are running, please try again later",
    HttpStatus.TOO_MANY_REQUESTS),
  BATCH_PARTIAL_COMPLETED(
    1815,
    "Job finished but the number of generated vouchers is less than requested",
    HttpStatus.MULTI_STATUS);
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    VoucherErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
