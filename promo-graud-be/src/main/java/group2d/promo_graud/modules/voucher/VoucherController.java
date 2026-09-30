package group2d.promo_graud.modules.voucher;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.voucher.dto.requests.UpdatedVoucherRequest;
import group2d.promo_graud.modules.voucher.dto.requests.VoucherRequest;
import group2d.promo_graud.modules.voucher.dto.responses.CreateVoucherResponse;
import group2d.promo_graud.modules.voucher.dto.responses.VoucherResponse;
import group2d.promo_graud.shared.dto.ApiResponse;
import group2d.promo_graud.shared.dto.PaginatedResponse;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<Voucher>> getVouchers(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer ruleId,
            @RequestParam(required = false) String distributionChannel,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(
                voucherService.getAll(type, status, ruleId, distributionChannel, page, pageSize));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateVoucherResponse>> createVoucher(
            @Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<CreateVoucherResponse>builder()
                        .code(200)
                        .message("Request success")
                        .result(voucherService.create(request))
                        .build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @Valid @RequestBody UpdatedVoucherRequest request, @PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.<VoucherResponse>builder()
                        .code(200)
                        .message("Request success")
                        .result(voucherService.update(id, request))
                        .build());
    }
}
