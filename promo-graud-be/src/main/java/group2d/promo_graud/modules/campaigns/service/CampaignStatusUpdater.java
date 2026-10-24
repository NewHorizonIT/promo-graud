package group2d.promo_graud.modules.campaigns.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.campaigns.CampaignRepository;
import group2d.promo_graud.modules.campaigns.CampaignStatus;

@Service
@RequiredArgsConstructor
public class CampaignStatusUpdater {
    private final CampaignRepository campaignRepository;

    @Transactional
    public void activate(Integer id) {
        campaignRepository.activateByIdStrict(
                id, CampaignStatus.UPCOMING, CampaignStatus.ACTIVE, LocalDateTime.now());
    }

    @Transactional
    public void end(Integer id) {
        campaignRepository.endByIdStrict(
                id, CampaignStatus.ACTIVE, CampaignStatus.ENDED, LocalDateTime.now());
    }
}
