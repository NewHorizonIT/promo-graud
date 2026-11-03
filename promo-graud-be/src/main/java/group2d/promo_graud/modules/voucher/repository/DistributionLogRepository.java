package group2d.promo_graud.modules.voucher.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import group2d.promo_graud.modules.voucher.entity.DistributionLog;
import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.DistributionLogStatus;

@Repository
public interface DistributionLogRepository extends JpaRepository<DistributionLog, Integer> {
    @Modifying
    @Transactional
    @Query(
            """
                  UPDATE DistributionLog d
                  SET d.status = :status,
                      d.retryCount = d.retryCount + 1,
                      d.lastAttemptAt = :time,
                      d.errorMessage = :error
                      WHERE d.id = :id
                  """)
    void markAsFailedAndIncrementRetry(
            @Param("id") Integer id,
            @Param("status") DistributionLogStatus status,
            @Param("time") LocalDateTime time,
            @Param("error") String error);

    @Modifying
    @Transactional
    @Query("UPDATE DistributionLog d SET d.status = :status WHERE d.id = :logId")
    int updateStatus(@Param("logId") Integer logId, @Param("status") DistributionLogStatus status);

    @Query(
            "SELECT d FROM DistributionLog d "
                    + "WHERE (:recipient = '' OR LOWER(d.recipient) LIKE LOWER(CONCAT('%', :recipient, '%'))) "
                    + "AND (:status IS NULL OR d.status = :status) "
                    + "AND (:retryCount IS null OR d.retryCount = :retryCount) "
                    + "AND (:channel IS null OR d.channel = :channel) ")
    Page<DistributionLog> getAll(
            @Param("recipient") String recipient,
            @Param("status") DistributionLogStatus status,
            @Param("retryCount") Integer retryCount,
            @Param("channel") DistributionChannel distributionChannel,
            Pageable pageable);
}
