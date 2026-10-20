package group2d.promo_graud.modules.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.analytics.dto.CampaignStatsResponse;
import group2d.promo_graud.modules.analytics.dto.OverviewStatsResponse;
import group2d.promo_graud.modules.analytics.dto.RuleBreakdownResponse;
import group2d.promo_graud.modules.analytics.dto.TimelinePointResponse;
import group2d.promo_graud.modules.analytics.dto.TopUserResponse;
import group2d.promo_graud.modules.analytics.dto.VoucherUtilizationResponse;
import group2d.promo_graud.modules.campaigns.Campaign;
import group2d.promo_graud.modules.campaigns.CampaignErrorCode;
import group2d.promo_graud.modules.campaigns.CampaignStatus;
import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;
import group2d.promo_graud.shared.exception.AppException;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    @Cacheable("analytics:overview")
    public OverviewStatsResponse getOverview() {
        return OverviewStatsResponse.builder()
                .totalCampaigns(analyticsRepository.countTotalCampaigns())
                .activeCampaigns(analyticsRepository.countCampaignsByStatus(CampaignStatus.ACTIVE))
                .totalVouchersIssued(analyticsRepository.sumTotalVouchersIssued())
                .totalRedeemed(analyticsRepository.countAllRedeemed(OrderStatus.SUCCESS))
                .totalDiscountGiven(
                        nullSafe(analyticsRepository.sumAllDiscount(OrderStatus.SUCCESS)))
                .totalRevenue(nullSafe(analyticsRepository.sumAllRevenue(OrderStatus.SUCCESS)))
                .build();
    }

    @Cacheable(value = "analytics:campaign-stats", key = "#campaignId")
    public CampaignStatsResponse getCampaignStats(Integer campaignId) {
        findCampaignOrThrow(campaignId);

        long issued = analyticsRepository.countIssuedByCampaign(campaignId);
        long redeemed =
                analyticsRepository.countRedeemedByCampaign(campaignId, OrderStatus.SUCCESS);
        BigDecimal discount =
                nullSafe(
                        analyticsRepository.sumDiscountByCampaign(campaignId, OrderStatus.SUCCESS));
        BigDecimal revenue =
                nullSafe(analyticsRepository.sumRevenueByCampaign(campaignId, OrderStatus.SUCCESS));

        double rate =
                issued == 0
                        ? 0.0
                        : BigDecimal.valueOf(redeemed * 100.0 / issued)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue();

        return CampaignStatsResponse.builder()
                .totalIssued(issued)
                .totalRedeemed(redeemed)
                .redemptionRate(rate)
                .totalDiscount(discount)
                .totalRevenue(revenue)
                .build();
    }

    @Cacheable(
            value = "analytics:timeline",
            key = "#campaignId + ':' + #from + ':' + #to + ':' + #interval")
    public List<TimelinePointResponse> getTimeline(
            Integer campaignId, LocalDateTime from, LocalDateTime to, String interval) {

        Campaign campaign = findCampaignOrThrow(campaignId);

        LocalDateTime resolvedFrom = from != null ? from : campaign.getStartTime();
        LocalDateTime resolvedTo = to != null ? to : LocalDateTime.now();

        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new AppException(CampaignErrorCode.END_TIME_MUST_BE_AFTER_START_TIME);
        }

        return analyticsRepository
                .getTimeline(campaignId, resolvedFrom, resolvedTo, interval)
                .stream()
                .map(
                        row ->
                                TimelinePointResponse.builder()
                                        .timestamp(((Timestamp) row[0]).toLocalDateTime())
                                        .redeemed(((Number) row[1]).longValue())
                                        .revenue(
                                                row[2] != null
                                                        ? new BigDecimal(row[2].toString())
                                                        : BigDecimal.ZERO)
                                        .build())
                .toList();
    }

    @Cacheable(value = "analytics:rule-breakdown", key = "#campaignId")
    public List<RuleBreakdownResponse> getRuleBreakdown(Integer campaignId) {
        findCampaignOrThrow(campaignId);

        return analyticsRepository.getRuleBreakdown(campaignId, OrderStatus.SUCCESS).stream()
                .map(
                        p ->
                                RuleBreakdownResponse.builder()
                                        .ruleId(p.getRuleId())
                                        .ruleName(p.getRuleName())
                                        .typeOfRule(TypeOfRule.valueOf(p.getTypeOfRule()))
                                        .redemptionCount(p.getRedemptionCount())
                                        .totalDiscount(nullSafe(p.getTotalDiscount()))
                                        .build())
                .toList();
    }

    @Cacheable(value = "analytics:top-users", key = "#campaignId + ':' + #limit")
    public List<TopUserResponse> getTopUsers(Integer campaignId, int limit) {
        findCampaignOrThrow(campaignId);

        return analyticsRepository
                .getTopUsers(campaignId, OrderStatus.SUCCESS, PageRequest.of(0, limit))
                .stream()
                .map(
                        p ->
                                TopUserResponse.builder()
                                        .userId(p.getUserId())
                                        .username(p.getUsername())
                                        .redemptionCount(p.getRedemptionCount())
                                        .totalSpent(nullSafe(p.getTotalSpent()))
                                        .build())
                .toList();
    }

    @Cacheable(
            value = "analytics:voucher-utilization",
            key = "#campaignId + ':' + #sortBy + ':' + #sortDir")
    public List<VoucherUtilizationResponse> getVoucherUtilization(
            Integer campaignId, String sortBy, String sortDir) {
        findCampaignOrThrow(campaignId);

        List<VoucherUtilizationResponse> result =
                analyticsRepository.getVoucherUtilization(campaignId).stream()
                        .map(
                                p -> {
                                    double usageRate =
                                            p.getQuantity() == 0
                                                    ? 0.0
                                                    : BigDecimal.valueOf(
                                                                    (p.getQuantity()
                                                                                    - p
                                                                                            .getQuantityRemain())
                                                                            * 100.0
                                                                            / p.getQuantity())
                                                            .setScale(2, RoundingMode.HALF_UP)
                                                            .doubleValue();
                                    return VoucherUtilizationResponse.builder()
                                            .voucherId(p.getVoucherId())
                                            .code(p.getCode())
                                            .type(VoucherType.valueOf(p.getType()))
                                            .quantity(p.getQuantity())
                                            .quantityRemain(p.getQuantityRemain())
                                            .usageRate(usageRate)
                                            .status(VoucherStatus.valueOf(p.getStatus()))
                                            .build();
                                })
                        .collect(java.util.stream.Collectors.toList());

        Comparator<VoucherUtilizationResponse> comparator =
                "quantity_remain".equals(sortBy)
                        ? Comparator.comparingInt(VoucherUtilizationResponse::getQuantityRemain)
                        : Comparator.comparingDouble(VoucherUtilizationResponse::getUsageRate);

        if ("asc".equalsIgnoreCase(sortDir)) {
            result.sort(comparator);
        } else {
            result.sort(comparator.reversed());
        }

        return result;
    }

    private Campaign findCampaignOrThrow(Integer id) {
        return analyticsRepository
                .findActiveCampaignById(id)
                .orElseThrow(() -> new AppException(CampaignErrorCode.CAMPAIGN_NOT_FOUND));
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return Objects.requireNonNullElse(value, BigDecimal.ZERO);
    }
}
