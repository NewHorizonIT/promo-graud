package group2d.promo_graud.modules.analytics.dto;

import lombok.Builder;
import lombok.Getter;

import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;

@Getter
@Builder
public class VoucherUtilizationResponse {
    private int voucherId;
    private String code;
    private VoucherType type;
    private int quantity;
    private int quantityRemain;
    private double usageRate;
    private VoucherStatus status;
}
