package group2d.promo_graud.modules.campaigns.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.campaigns.CampaignRepository;
import group2d.promo_graud.modules.campaigns.CampaignStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignScheduleService {
    private final CampaignRepository campaignRepository;

    @Scheduled(cron = "0 */30 * * * ?")
    @Transactional
    public void syncCampaignStatuses() {
        log.info("[SCHEDULE] Bắt đầu tiến trình quét chiến dịch hết hạn");

        LocalDateTime now = LocalDateTime.now();
        int activated =
                campaignRepository.activateUpcoming(
                        CampaignStatus.UPCOMING, CampaignStatus.ACTIVE, now);
        int ended =
                campaignRepository.updateStatusForExpired(
                        CampaignStatus.ACTIVE, CampaignStatus.ENDED, now);

        if (activated > 0 || ended > 0) {
            log.info(
                    "[SCHEDULE] Kích hoạt {} chiến dịch, kết thúc {} chiến dịch", activated, ended);
        }
    }
}
