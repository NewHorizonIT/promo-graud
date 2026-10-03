package group2d.promo_graud.modules.voucher.dto.responses;

import group2d.promo_graud.modules.voucher.enums.VoucherType;
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
      boolean isGeneric = v.getType() == VoucherType.GENERIC;
        return VoucherResponse.builder()
                .id(v.getId())
                .code(v.getCode())
                .type(v.getType().name())
                .ruleId(v.getRuleCampaign() != null ? v.getRuleCampaign().getId() : null)
                .quantity(isGeneric ? v.getQuantity() : null)
                .quantityRemain(isGeneric ? v.getQuantityRemain() : null)
                .limitClient(isGeneric ? v.getQuantityRemain() : null)
                .distributionChannel(v.getDistributionChannel().name())
                .status(v.getStatus().name())
                .build();
    }
}
