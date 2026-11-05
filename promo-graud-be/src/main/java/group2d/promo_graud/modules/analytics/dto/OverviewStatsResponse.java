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
public class OverviewStatsResponse {
    private long totalCampaigns;
    private long activeCampaigns;
    private long totalVouchersIssued;
    private long totalRedeemed;
    private BigDecimal totalDiscountGiven;
    private BigDecimal totalRevenue;
}
