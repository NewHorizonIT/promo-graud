package group2d.promo_graud.modules.analytics.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUserResponse {
    private int userId;
    private String username;
    private long redemptionCount;
    private BigDecimal totalSpent;
}
