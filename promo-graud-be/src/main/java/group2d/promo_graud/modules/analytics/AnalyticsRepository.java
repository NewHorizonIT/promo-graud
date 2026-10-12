package group2d.promo_graud.modules.analytics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import group2d.promo_graud.modules.analytics.projection.RuleBreakdownProjection;
import group2d.promo_graud.modules.analytics.projection.TopUserProjection;
import group2d.promo_graud.modules.analytics.projection.VoucherUtilizationProjection;
import group2d.promo_graud.modules.campaigns.Campaign;
import group2d.promo_graud.modules.campaigns.CampaignStatus;
import group2d.promo_graud.modules.orders.OrderStatus;

public interface AnalyticsRepository extends Repository<Campaign, Integer> {

    @Query("SELECT c FROM Campaign c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Campaign> findActiveCampaignById(@Param("id") Integer id);

    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.isDeleted = false")
    long countTotalCampaigns();

    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.status = :status AND c.isDeleted = false")
    long countCampaignsByStatus(@Param("status") CampaignStatus status);

    @Query(
            "SELECT COALESCE(SUM(v.quantity), 0) FROM Voucher v JOIN v.ruleCampaign rc WHERE rc.isDeleted = false")
    long sumTotalVouchersIssued();

    @Query("SELECT COUNT(vr) FROM VoucherRedemption vr WHERE vr.statusOfOrder = :status")
    long countAllRedeemed(@Param("status") OrderStatus status);

    @Query(
            """
            SELECT COALESCE(SUM(o.totalPrice - o.finalPrice), 0)
            FROM VoucherRedemption vr JOIN vr.order o
            WHERE vr.statusOfOrder = :status
            """)
    BigDecimal sumAllDiscount(@Param("status") OrderStatus status);

    @Query(
            """
            SELECT COALESCE(SUM(o.finalPrice), 0)
            FROM VoucherRedemption vr JOIN vr.order o
            WHERE vr.statusOfOrder = :status
            """)
    BigDecimal sumAllRevenue(@Param("status") OrderStatus status);

    @Query(
            """
            SELECT COALESCE(SUM(v.quantity), 0)
            FROM Voucher v JOIN v.ruleCampaign rc
            WHERE rc.campaign.id = :id AND rc.isDeleted = false
            """)
    long countIssuedByCampaign(@Param("id") Integer campaignId);

    @Query(
            """
            SELECT COUNT(vr)
            FROM VoucherRedemption vr
            JOIN vr.voucher v JOIN v.ruleCampaign rc
            WHERE rc.campaign.id = :id AND vr.statusOfOrder = :status
            """)
    long countRedeemedByCampaign(
            @Param("id") Integer campaignId, @Param("status") OrderStatus status);

    @Query(
            """
            SELECT COALESCE(SUM(o.totalPrice - o.finalPrice), 0)
            FROM VoucherRedemption vr
            JOIN vr.order o JOIN vr.voucher v JOIN v.ruleCampaign rc
            WHERE rc.campaign.id = :id AND vr.statusOfOrder = :status
            """)
    BigDecimal sumDiscountByCampaign(
            @Param("id") Integer campaignId, @Param("status") OrderStatus status);

    @Query(
            """
            SELECT COALESCE(SUM(o.finalPrice), 0)
            FROM VoucherRedemption vr
            JOIN vr.order o JOIN vr.voucher v JOIN v.ruleCampaign rc
            WHERE rc.campaign.id = :id AND vr.statusOfOrder = :status
            """)
    BigDecimal sumRevenueByCampaign(
            @Param("id") Integer campaignId, @Param("status") OrderStatus status);

    @Query(
            """
            SELECT rc.id          AS ruleId,
                   rc.name        AS ruleName,
                   rc.typeOfRule  AS typeOfRule,
                   COUNT(vr)      AS redemptionCount,
                   COALESCE(SUM(o.totalPrice - o.finalPrice), 0) AS totalDiscount
            FROM VoucherRedemption vr
            JOIN vr.order o
            JOIN vr.voucher v JOIN v.ruleCampaign rc
            WHERE rc.campaign.id = :id AND vr.statusOfOrder = :status
            GROUP BY rc.id, rc.name, rc.typeOfRule
            ORDER BY COUNT(vr) DESC
            """)
    List<RuleBreakdownProjection> getRuleBreakdown(
            @Param("id") Integer campaignId, @Param("status") OrderStatus status);

    @Query(
            """
            SELECT vr.user.id       AS userId,
                   vr.user.username AS username,
                   COUNT(vr)        AS redemptionCount,
                   COALESCE(SUM(o.finalPrice), 0) AS totalSpent
            FROM VoucherRedemption vr
            JOIN vr.order o
            JOIN vr.voucher v JOIN v.ruleCampaign rc
            WHERE rc.campaign.id = :id AND vr.statusOfOrder = :status
            GROUP BY vr.user.id, vr.user.username
            ORDER BY COUNT(vr) DESC
            """)
    List<TopUserProjection> getTopUsers(
            @Param("id") Integer campaignId,
            @Param("status") OrderStatus status,
            Pageable pageable);

    @Query(
            """
            SELECT v.id             AS voucherId,
                   v.code           AS code,
                   v.type           AS type,
                   v.quantity       AS quantity,
                   v.quantityRemain AS quantityRemain,
                   v.status         AS status
            FROM Voucher v JOIN v.ruleCampaign rc
            WHERE rc.campaign.id = :id AND rc.isDeleted = false
            """)
    List<VoucherUtilizationProjection> getVoucherUtilization(@Param("id") Integer campaignId);

    @Query(
            value =
                    """
            SELECT DATE_TRUNC(:interval, o.created_at) AS timestamp,
                   COUNT(vr.id)                         AS redeemed,
                   COALESCE(SUM(o.final_price), 0)      AS revenue
            FROM voucher_redemption vr
            JOIN "order" o        ON vr.order_id   = o.id
            JOIN voucher v        ON vr.voucher_id = v.id
            JOIN rule_campaign rc ON v.rule_id     = rc.id
            WHERE rc.campaign_id     = :campaignId
              AND vr.status_of_order = 'SUCCESS'
              AND o.created_at BETWEEN :from AND :to
            GROUP BY 1
            ORDER BY 1
            """,
            nativeQuery = true)
    List<Object[]> getTimeline(
            @Param("campaignId") Integer campaignId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("interval") String interval);
}
