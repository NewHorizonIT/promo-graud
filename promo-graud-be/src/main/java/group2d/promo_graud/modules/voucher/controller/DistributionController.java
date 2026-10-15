package group2d.promo_graud.modules.voucher.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.voucher.dto.requests.DistributeVoucherRequest;
import group2d.promo_graud.modules.voucher.service.DistributeVoucherService;
import group2d.promo_graud.shared.dto.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/distributes")
public class DistributionController {
    private final DistributeVoucherService distributeVoucherService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> distributeVouchers(
            @Valid @RequestBody DistributeVoucherRequest request) {
        Long jobId = distributeVoucherService.distribute(request);
        return ResponseEntity.ok(
                ApiResponse.<Long>builder()
                        .code(200)
                        .message("Request success: Job đang chạy ngầm")
                        .result(jobId)
                        .build());
    }
}
