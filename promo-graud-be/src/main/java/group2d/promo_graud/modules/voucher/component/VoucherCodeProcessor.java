package group2d.promo_graud.modules.voucher.component;

import java.util.UUID;

import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.RuleCampaignRepository;
import group2d.promo_graud.modules.rules.enums.RuleCampaignErrorCode;
import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.VoucherType;
import group2d.promo_graud.shared.exception.AppException;

@Component
@StepScope
@RequiredArgsConstructor
// Đổi VoucherRow thành Entity Voucher của bạn
public class VoucherCodeProcessor implements ItemProcessor<Integer, Voucher> {
    private RuleCampaign cachedCampaign;

    @Value("#{jobParameters['ruleId']}")
    private Long ruleId;

    @Value("#{jobParameters['limitClient']}")
    private Long limitClient;

    @Value("#{jobParameters['channel']}")
    private String channel;

    @Value("#{jobParameters['prefix']}")
    private String prefix;

    private final RuleCampaignRepository ruleCampaignRepository;

    @BeforeStep
    public void beforeStep() {
        this.cachedCampaign =
                ruleCampaignRepository
                        .findById(ruleId.intValue())
                        .orElseThrow(() -> new AppException(RuleCampaignErrorCode.RULE_NOT_EXIST));
    }

    @Override
    public Voucher process(Integer idx) {
        Voucher voucher =
                Voucher.builder()
                        .code(generateCode(prefix))
                        .limitClient(limitClient.intValue())
                        .ruleCampaign(cachedCampaign)
                        .quantity(1)
                        .quantityRemain(1)
                        .distributionChannel(DistributionChannel.valueOf(channel))
                        .type(VoucherType.UNIQUE)
                        .build();
        return voucher;
    }

    private String generateCode(String prefix) {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return StringUtils.hasText(prefix) ? prefix + "-" + random : random;
    }
}
