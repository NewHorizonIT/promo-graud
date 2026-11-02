package group2d.promo_graud.modules.user.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public enum UserVoucherErrorCode implements BaseErrorCode {
    USER_VOUCHER_NOT_EXISTS(1600, "User_Voucher not exists", HttpStatus.NOT_FOUND),
    USER_VOUCHER_ALREADY_USED(1601, "User_Voucherd đã được sử dụng", HttpStatus.CONFLICT),
    USER_VOUCHER_ALREADY_CLAIMED(1602, "Bạn đã lưu voucher này rồi", HttpStatus.CONFLICT),
    USER_VOUCHER_INACTIVE(1603, "Voucher không hoạt động", HttpStatus.BAD_REQUEST),
    USER_VOUCHER_VOUCHER_OUT_STOCK(1604, "Voucher đã hết", HttpStatus.BAD_REQUEST);
    int code;
    String message;
    HttpStatusCode statusCode;
}
