package group2d.promo_graud.modules.rules.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import lombok.Data;

import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;

@Data
public class RuleUpdateRequest {

    @Size(max = 255, message = "Tên rule tối đa 255 ký tự")
    private String name;

    private Integer campaignId;

    private TypeOfRule typeOfRule;

    @Min(value = 0, message = "Giá trị phải lớn hơn hoặc bằng 0")
    private BigDecimal value;

    @Min(value = 0, message = "Giá trị giảm tối đa phải lớn hơn hoặc bằng 0")
    private BigDecimal maxDiscountValue;

    @Min(value = 0, message = "Giá trị đơn hàng tối thiểu phải lớn hơn hoặc bằng 0")
    private BigDecimal minOrderValue;

    private UserTypeEnum typeOfUser;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Map<String, Object> payload;
}
