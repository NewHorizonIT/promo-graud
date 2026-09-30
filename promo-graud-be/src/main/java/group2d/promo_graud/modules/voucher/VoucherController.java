package group2d.promo_graud.modules.voucher;

import group2d.promo_graud.modules.voucher.dto.requests.UpdatedVoucherRequest;
import group2d.promo_graud.modules.voucher.dto.requests.VoucherRequest;
import group2d.promo_graud.modules.voucher.dto.responses.CreateVoucherResponse;
import group2d.promo_graud.modules.voucher.dto.responses.VoucherResponse;
import group2d.promo_graud.shared.dto.ApiResponse;
import group2d.promo_graud.shared.dto.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {
  private final VoucherService voucherService;
  @GetMapping
  public ResponseEntity<PaginatedResponse<Voucher>> getVouchers(
    @RequestParam(required = false) String type,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) Integer rule_id,
    @RequestParam(required = false) String distributionChannel,
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "20") int page_size
  ) {
    return ResponseEntity.ok(voucherService.getAll(type, status,rule_id,distributionChannel, page, page_size));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CreateVoucherResponse>> createVoucher(
    @Valid @RequestBody VoucherRequest request
    ){
    return ResponseEntity.ok(ApiResponse.<CreateVoucherResponse>builder()
        .code(200)
        .message("Request success")
        .result(voucherService.create(request))
      .build());
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
    @Valid @RequestBody UpdatedVoucherRequest request,
    @RequestParam("id") Integer id
  ){
    return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
      .code(200)
      .message("Request success")
      .result(voucherService.update(id,request))
      .build());
  }
}
