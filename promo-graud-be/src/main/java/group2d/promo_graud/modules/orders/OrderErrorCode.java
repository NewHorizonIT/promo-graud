package group2d.promo_graud.modules.orders;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum OrderErrorCode implements BaseErrorCode {
    ORDER_NOT_EXISTS(1300, "Order not exists", HttpStatus.BAD_REQUEST),
    ORDER_ITEMS_NOT_FOUND(1301, "Order items không tìm thấy", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_CANCEL(1302, "Đơn hàng đã hủy, không thể hủy lại", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_DUPLICATE(
            1303, "trạng thái đơn bị trùng không thể update", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_DELETE(
            1304, "Không thể xóa nếu trạng thái còn pending hoặc success", HttpStatus.BAD_REQUEST),
    INVALID_IDEMPOTENCY_KEY(1305, "Key không đúng định dạng", HttpStatus.BAD_REQUEST),
    PENDING_PAYMENT_EXISTS(
            1307,
            "Bạn đang có đơn hàng chờ thanh toán. Vui lòng thanh toán hoặc hủy đơn đó trước",
            HttpStatus.CONFLICT),
    ORDER_STATUS(1306, "Đơn hàng chưa thanh toán không thể sửa trạng thái", HttpStatus.BAD_REQUEST);
    int code;
    String message;
    HttpStatusCode statusCode;
}
