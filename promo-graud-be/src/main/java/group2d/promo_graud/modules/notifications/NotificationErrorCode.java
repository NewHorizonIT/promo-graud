package group2d.promo_graud.modules.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {
    NOTIFICATION_NOT_FOUND(1901, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND),
    NOTIFICATION_CLIENT_NOT_REACH(
            1902, "Không được gửi message vào server", HttpStatus.UNAUTHORIZED),
    NOTIFICATION_NOT_REACH(1903, "Khong the ket noi ws", HttpStatus.BAD_REQUEST);
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
