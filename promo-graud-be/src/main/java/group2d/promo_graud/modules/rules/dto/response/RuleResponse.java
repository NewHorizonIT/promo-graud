package group2d.promo_graud.modules.rules.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.enums.RuleStatus;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;

@Data
@Builder
public class RuleResponse {
    private Integer id;
    private String name;
    private Integer campaignId;
    private TypeOfRule typeOfRule;
    private BigDecimal value;
    private BigDecimal maxDiscountValue;
    private BigDecimal minOrderValue;
    private UserTypeEnum typeOfUser;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, Object> payload;
    private RuleStatus status;
    private LocalDateTime createdAt;

    // Hàm tiện ích để chuyển từ Entity sang Response DTO
    public static RuleResponse fromEntity(RuleCampaign entity) {
        if (entity == null) {
            return null;
        }
        ;

        return RuleResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .campaignId(entity.getCampaign() != null ? entity.getCampaign().getId() : null)
                .typeOfRule(entity.getTypeOfRule())
                .value(entity.getValue())
                .maxDiscountValue(entity.getMaxDiscountValue())
                .minOrderValue(entity.getMinOrderValue())
                .typeOfUser(entity.getTypeOfUser())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .payload(entity.getPayload())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
