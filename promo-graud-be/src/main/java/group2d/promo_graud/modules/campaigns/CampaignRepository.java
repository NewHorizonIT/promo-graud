package group2d.promo_graud.modules.campaigns;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign,Integer> {

    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false " +
      "AND (:name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
      "AND (:hasStatus = false OR c.status = :status) " +
      "AND (:hasStartTime = false OR c.startTime <= :endTime) " +
      "AND (:hasEndTime = false OR c.endTime >= :startTime)"
    )
    Page<Campaign> searchAndFilterCampaign(
      @Param("name") String name,
      @Param("status") CampaignStatus status,
      @Param("hasStatus") boolean hasStatus, // Thêm cờ cho status

      @Param("startTime") LocalDateTime startTime,
      @Param("hasStartTime") boolean hasStartTime, // Thêm cờ cho startTime

      @Param("endTime") LocalDateTime endTime,
      @Param("hasEndTime") boolean hasEndTime, // Thêm cờ cho endTime

      Pageable pageable
    );


}
