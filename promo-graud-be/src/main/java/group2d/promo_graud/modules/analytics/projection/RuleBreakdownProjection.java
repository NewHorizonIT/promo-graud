package group2d.promo_graud.modules.analytics.projection;

import java.math.BigDecimal;

public interface RuleBreakdownProjection {
    Integer getRuleId();

    String getRuleName();

    String getTypeOfRule();

    Long getRedemptionCount();

    BigDecimal getTotalDiscount();
}
