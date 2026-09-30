package group2d.promo_graud.modules.voucher;

import group2d.promo_graud.modules.logs.DistributionStatus;
import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher,Integer> {
  @Query("""
        SELECT v FROM Voucher v
        WHERE (:type IS NULL OR v.type = :type)
        AND (:status IS NULL OR v.status = :status)
        AND (:ruleId IS NULL OR v.ruleCampaign.id = :ruleId)
        AND (:distributionChannel IS NULL OR v.distributionChannel = :distributionChannel)
    """)
  Page<Voucher> getAll(
    @Param("type") VoucherType type,
    @Param("status") VoucherStatus status,
    @Param("ruleId") Integer ruleId,
    @Param("distributionChannel") DistributionChannel distributionChannel,
    Pageable pageable
  );
}
