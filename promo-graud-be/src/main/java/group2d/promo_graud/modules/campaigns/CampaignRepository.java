package group2d.promo_graud.modules.campaigns;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository
        extends JpaRepository<Campaign, Integer>, JpaSpecificationExecutor<Campaign> {
    // Hàm dành cho Job kích hoạt lúc bắt đầu chiến dịch
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            """
    update Campaign c
    set c.status = :newStatus
    where c.id = :id
    and c.status = :oldStatus
    and c.startTime <= :now
            """)
    int activateByIdStrict(
            @Param("id") Integer id,
            @Param("oldStatus") CampaignStatus oldStatus,
            @Param("newStatus") CampaignStatus newStatus,
            @Param("now") LocalDateTime now);

    // 2. Hàm dành cho Job kết thúc (Check thêm endTime)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            """
    update Campaign c
       set c.status = :newStatus
     where c.id = :id
       and c.status = :oldStatus
       and c.endTime <= :now
            """)
    int endByIdStrict(
            @Param("id") Integer id,
            @Param("oldStatus") CampaignStatus oldStatus,
            @Param("newStatus") CampaignStatus newStatus,
            @Param("now") LocalDateTime now);

    // dùng cho cronjob
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            """
    update Campaign c
      set c.status = :newStatus
      where c.status = :oldStatus
      and c.endTime < :now
            """)
    int updateStatusForExpired(
            @Param("oldStatus") CampaignStatus oldStatus,
            @Param("newStatus") CampaignStatus newStatus,
            @Param("now") LocalDateTime now);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            """
    update Campaign c
       set c.status = :newStatus
     where c.status = :oldStatus
       and c.startTime <= :now
       and c.endTime > :now
            """)
    int activateUpcoming(
            @Param("oldStatus") CampaignStatus oldStatus,
            @Param("newStatus") CampaignStatus newStatus,
            @Param("now") LocalDateTime now);
}
