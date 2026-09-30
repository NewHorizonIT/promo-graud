package group2d.promo_graud.modules.voucher.dto.responses;

import group2d.promo_graud.modules.voucher.Voucher;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CreateVoucherResponse {
  private Integer generated;
  private List<Voucher> vouchers;
  private String status;   // "completed" | "processing"
  private Long jobId;      // null nếu completed
}
