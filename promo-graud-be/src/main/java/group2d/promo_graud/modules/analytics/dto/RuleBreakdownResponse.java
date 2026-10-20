package group2d.promo_graud.modules.analytics.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

import group2d.promo_graud.modules.rules.enums.TypeOfRule;

@Getter
@Builder
public class RuleBreakdownResponse {
    private int ruleId;
    private String ruleName;
    private TypeOfRule typeOfRule;
    private long redemptionCount;
    private BigDecimal totalDiscount;
}
