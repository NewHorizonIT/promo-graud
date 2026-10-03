package group2d.promo_graud.modules.rules;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.rules.enums.RuleStatus;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;

@Repository
public interface RuleCampaignRepository extends JpaRepository<RuleCampaign, Integer> {
    @Query(
            "SELECT r FROM RuleCampaign r "
                    + "WHERE r.isDeleted = false "
                    + "AND LOWER(r.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%'))"
                    + "AND (:type IS NULL OR r.typeOfRule = :type) "
                    + "AND (:status IS NULL OR r.status = :status) "
                    + "AND (:campaignId IS NULL OR r.campaign.id = :campaignId)")
    Page<RuleCampaign> getAllByFilter(
            @Param("name") String name,
            @Param("type") TypeOfRule type,
            @Param("status") RuleStatus status,
            @Param("campaignId") Integer campaignId,
            Pageable pageable);
}
