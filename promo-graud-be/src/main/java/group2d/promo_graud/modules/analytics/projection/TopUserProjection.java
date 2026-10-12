package group2d.promo_graud.modules.analytics.projection;

import java.math.BigDecimal;

public interface TopUserProjection {
    Integer getUserId();

    String getUsername();

    Long getRedemptionCount();

    BigDecimal getTotalSpent();
}
