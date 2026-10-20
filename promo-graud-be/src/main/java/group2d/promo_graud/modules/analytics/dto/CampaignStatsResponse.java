package group2d.promo_graud.modules.analytics.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampaignStatsResponse {
    private long totalIssued;
    private long totalRedeemed;
    private double redemptionRate;
    private BigDecimal totalDiscount;
    private BigDecimal totalRevenue;
}
