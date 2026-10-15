package group2d.promo_graud.modules.voucher.component.distribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import group2d.promo_graud.modules.voucher.enums.DistributionChannel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributionPayload {
    private Integer logId;
    private String userEmail;
    private String voucherCode;
    private DistributionChannel channel;
    private String targetUrl;
}
