package group2d.promo_graud.modules.analytics.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OverviewStatsResponse {
    private long totalCampaigns;
    private long activeCampaigns;
    private long totalVouchersIssued;
    private long totalRedeemed;
    private BigDecimal totalDiscountGiven;
    private BigDecimal totalRevenue;
}
