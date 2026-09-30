package group2d.promo_graud.modules.voucher.dto.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import group2d.promo_graud.modules.voucher.Voucher;

@Getter
@Setter
@Builder
public class VoucherResponse {
    private Integer id;
    private String code;
    private String type;
    private Integer ruleId;
    private Integer quantity;
    private Integer quantityRemain;
    private Integer limitClient;
    private String distributionChannel;
    private String status;

    public static VoucherResponse fromEntity(Voucher v) {
        return VoucherResponse.builder()
                .id(v.getId())
                .code(v.getCode())
                .type(v.getType().name())
                .ruleId(v.getRuleCampaign() != null ? v.getRuleCampaign().getId() : null)
                .quantity(v.getQuantity())
                .quantityRemain(v.getQuantityRemain())
                .limitClient(v.getLimitClient())
                .distributionChannel(v.getDistributionChannel().name())
                .status(v.getStatus().name())
                .build();
    }
}
