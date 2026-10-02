package group2d.promo_graud.modules.voucher.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.VoucherType;

@Data
public class VoucherRequest {

    @NotNull private VoucherType type;

    private Integer ruleId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull private String prefix;

    @Min(1)
    private Integer limitClient = 1;

    @NotNull private DistributionChannel distributionChannel;
}
