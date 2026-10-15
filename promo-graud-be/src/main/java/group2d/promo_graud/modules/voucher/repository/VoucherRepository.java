package group2d.promo_graud.modules.voucher.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.voucher.dto.responses.VoucherCardResponse;
import group2d.promo_graud.modules.voucher.entity.Voucher;
import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    @Query(
            "SELECT v FROM Voucher v "
                    + "WHERE (:type IS NULL OR v.type = :type) "
                    + "AND (:status IS NULL OR v.status = :status) "
                    + "AND (:ruleId IS NULL OR v.ruleCampaign.id = :ruleId) "
                    + "AND (:distributionChannel IS NULL OR v.distributionChannel = :distributionChannel)")
    Page<Voucher> getAll(
            @Param("type") VoucherType type,
            @Param("status") VoucherStatus status,
            @Param("ruleId") Integer ruleId,
            @Param("distributionChannel") DistributionChannel distributionChannel,
            Pageable pageable);

    // interface base projection
    // map tự động vào Interface Projection nhờ các alias (AS) trùng tên với Getter
    @Query(
            "SELECT r.id AS ruleId, r.name AS ruleName, c.name AS campaignName, "
                    + "v.distributionChannel AS channel, COUNT(v.id) AS totalUniqueVouchers "
                    + "FROM Voucher v JOIN v.ruleCampaign r JOIN r.campaign c "
                    + "WHERE v.type = 'UNIQUE' AND v.status = 'ACTIVE' "
                    + "GROUP BY r.id, r.name, c.name, v.distributionChannel")
    List<VoucherCardResponse> getUniqueVoucherCard();

    Long countByRuleCampaign_IdAndTypeAndStatusAndDistributionChannel(
            Integer ruleId, VoucherType type, VoucherStatus status, DistributionChannel channel);

    // ngăn tình trạng 2 luồng cùng rút cùng 1 mã dùng Permissive Lock
    // Rút 1 lúc N mã ACTIVE và KHÓA NÓ LẠI (SKIP LOCKED).
    // Bất kỳ luồng/server nào khác gọi vào sẽ tự động bỏ qua mã này và lấy mã tiếp theo.
    @Query(
            value =
                    "SELECT * FROM Voucher WHERE rule_id = :ruleId AND type = 'UNIQUE' AND status = 'ACTIVE' AND distribution_channel = :channel LIMIT :limit FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<Voucher> findActiveVouchersWithLock(
            @Param("ruleId") Integer ruleId,
            @Param("channel") String channel,
            @Param("limit") int limit);

    @Modifying
    @Query("UPDATE Voucher v SET v.status = 'ALLOCATED' WHERE v.id IN :ids")
    void updateStatus(@Param("ids") List<Integer> ids);
}
