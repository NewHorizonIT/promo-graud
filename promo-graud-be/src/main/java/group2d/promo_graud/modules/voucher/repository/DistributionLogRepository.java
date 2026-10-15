package group2d.promo_graud.modules.voucher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.voucher.entity.DistributionLog;
import group2d.promo_graud.modules.voucher.enums.DistributionLogStatus;

@Repository
public interface DistributionLogRepository extends JpaRepository<DistributionLog, Integer> {
    @Modifying
    @Query(
            "UPDATE DistributionLog d SET d.status = :status, d.errorMessage = :error WHERE d.voucher.id = :voucherId AND d.recipient = :userId")
    void updateLogStatus(
            @Param("voucherId") Integer voucherId,
            @Param("userId") Integer userId,
            @Param("status") DistributionLogStatus status,
            @Param("error") String error);

    @Modifying
    @Query("UPDATE DistributionLog d SET d.status = :status WHERE d.id = :logId")
    int updateStatus(@Param("logId") Integer logId, @Param("status") DistributionLogStatus status);
}
