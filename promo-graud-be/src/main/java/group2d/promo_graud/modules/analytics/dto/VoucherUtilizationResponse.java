package group2d.promo_graud.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherUtilizationResponse {
    private int voucherId;
    private String code;
    private VoucherType type;
    private int quantity;
    private int quantityRemain;
    private double usageRate;
    private VoucherStatus status;
}
