package group2d.promo_graud.modules.voucher.dto.requests;

import jakarta.validation.constraints.Min;

import lombok.Data;

import group2d.promo_graud.modules.voucher.enums.VoucherStatus;

@Data
public class UpdatedVoucherRequest {
    private VoucherStatus status;

    @Min(0)
    private Integer quantityRemain;
}
