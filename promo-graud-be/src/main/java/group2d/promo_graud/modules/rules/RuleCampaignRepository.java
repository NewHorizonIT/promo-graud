package group2d.promo_graud.modules.rules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleCampaignRepository extends JpaRepository<RuleCampaign, Integer> {}
