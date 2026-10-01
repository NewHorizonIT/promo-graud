package group2d.promo_graud.modules.campaigns;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository
        extends JpaRepository<Campaign, Integer>, JpaSpecificationExecutor<Campaign> {}
