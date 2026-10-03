package group2d.promo_graud.modules.rules.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import jakarta.validation.constraints.*;

import lombok.Data;

import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;

@Data
public class RuleCreateRequest {
    @NotBlank(message = "Tên rule không được để trống")
    @Size(max = 255, message = "Tên rule tối đa 255 ký tự")
    private String name;

    @NotNull(message = "Campaign ID không được để trống")
    private Integer campaignId;

    @NotNull(message = "Loại rule không được để trống")
    private TypeOfRule typeOfRule;

    @Min(value = 0, message = "Giá trị (value) phải lớn hơn hoặc bằng 0")
    private BigDecimal value;

    @Min(value = 0, message = "Giá trị giảm tối đa phải lớn hơn hoặc bằng 0")
    private BigDecimal maxDiscountValue;

    @Min(value = 0, message = "Giá trị đơn hàng tối thiểu phải lớn hơn hoặc bằng 0")
    private BigDecimal minOrderValue;

    private UserTypeEnum typeOfUser = UserTypeEnum.NORMAL;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @FutureOrPresent(message = "Thời gian bắt đầu phải ở hiện tại hoặc tương lai")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @Future(message = "Thời gian kết thúc phải ở tương lai")
    private LocalDateTime endTime;

    private Map<String, Object> payload;
}
