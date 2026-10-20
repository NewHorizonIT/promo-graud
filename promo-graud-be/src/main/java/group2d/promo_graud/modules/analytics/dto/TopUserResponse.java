package group2d.promo_graud.modules.analytics.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopUserResponse {
    private int userId;
    private String username;
    private long redemptionCount;
    private BigDecimal totalSpent;
}
