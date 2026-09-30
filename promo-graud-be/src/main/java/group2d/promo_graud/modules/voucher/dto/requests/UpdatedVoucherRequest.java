package group2d.promo_graud.modules.voucher.dto.requests;

import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdatedVoucherRequest {
  private VoucherStatus status;

  @Min(0)
  private Integer quantityRemain;
}
