package group2d.promo_graud.modules.voucher.dto.requests;

import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.VoucherType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
public class VoucherRequest {

  @NotBlank
  private VoucherType type;

  @NotNull
  private Integer ruleId;

  @NotNull
  @Min(1)
  private Integer quantity;

  @NotNull
  private String prefix;

  @Min(1)
  private Integer limitClient = 1;

  @NotNull
  private LocalDateTime expiredAt;

  @NotBlank
  private DistributionChannel distributionChannel;
}
