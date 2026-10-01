package group2d.promo_graud.modules.campaigns;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import group2d.promo_graud.shared.exception.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum CampaignErrorCode implements BaseErrorCode {
    // --- 1. Nhóm Tìm kiếm & Tồn tại ---
    CAMPAIGN_NOT_FOUND(1201, "Không tìm thấy chiến dịch", HttpStatus.NOT_FOUND),
    CAMPAIGN_IS_DELETED(1202, "Chiến dịch đang bị xóa mềm", HttpStatus.NOT_FOUND),

    // --- 2. Nhóm Validation Thời gian & Ngân sách ---
    INVALID_CAMPAIGN_TIME(
            1203, "Thời gian bắt đầu phải trước thời gian kết thúc", HttpStatus.BAD_REQUEST),
    CAMPAIGN_IN_PAST(1204, "Thời gian không được nằm trong quá khứ", HttpStatus.BAD_REQUEST),
    INVALID_CAMPAIGN_BUDGET(1205, "Ngân sách chiến dịch phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    END_TIME_MUST_BE_IN_FUTURE(1206, "Endtime phải lớn hơn hiện tại", HttpStatus.BAD_REQUEST),
    CAMPAIGN_BUDGET_NOT_NULL(1207, "ngân sách khuyến mãi không được rỗng", HttpStatus.BAD_REQUEST),

    // --- 3. Nhóm Trạng thái & Vòng đời ---
    CAMPAIGN_END(1208, "Chiến dịch đã hết hạn không thể cập nhật được nữa", HttpStatus.BAD_REQUEST),
    CANNOT_ACTIVATE_BEFORE_START_TIME(
            1209, "Chưa đến giờ bắt đầu chiến dịch", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE_RUNNING_CAMPAIGN(
            1210,
            "Không thể sửa thời gian bắt đầu khi chiến dịch đang hoạt động",
            HttpStatus.BAD_REQUEST),
    INVALID_TIME_FILTER(
            1211, "Vui lòng chọn cả thời gian bắt đầu và kết thúc để lọc", HttpStatus.BAD_REQUEST),
    END_TIME_MUST_BE_AFTER_START_TIME(
            1212, "thời gian kết thúc phải hơn thời gian bắt dầu", HttpStatus.BAD_REQUEST),
    // --- 4. Nhóm Ràng buộc Thao tác ---
    CANNOT_DELETE_UNENDED_CAMPAIGN(
            1213, "Chỉ có thể xóa chiến dịch đã END", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
