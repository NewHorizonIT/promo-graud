package group2d.promo_graud.modules.voucher.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.voucher.dto.requests.DistributeVoucherRequest;
import group2d.promo_graud.modules.voucher.dto.responses.DistributionResponse;
import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.DistributionLogStatus;
import group2d.promo_graud.modules.voucher.service.DistributeVoucherService;
import group2d.promo_graud.shared.dto.ApiResponse;
import group2d.promo_graud.shared.dto.PaginatedResponse;

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

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<DistributionResponse>>> getDistributions(
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false) DistributionLogStatus status,
            @RequestParam(required = false) DistributionChannel channel,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "1") int page) {
        Page<DistributionResponse> pages =
                distributeVoucherService.getAllDistribution(recipient, status, channel, size, page);
        PaginatedResponse<DistributionResponse> result =
                PaginatedResponse.<DistributionResponse>builder()
                        .data(pages.getContent())
                        .page(pages.getNumber() + 1)
                        .totalItems(pages.getTotalElements())
                        .totalPages(pages.getTotalPages())
                        .build();

        return ResponseEntity.ok(
                ApiResponse.<PaginatedResponse<DistributionResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách log phân phối thành công")
                        .result(result)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DistributionResponse>> getDistributionById(
            @PathVariable Integer id) {
        DistributionResponse response = distributeVoucherService.getDistributionById(id);
        return ResponseEntity.ok(
                ApiResponse.<DistributionResponse>builder()
                        .code(200)
                        .message("Lấy chi tiết thông tin Log phân phối thành công")
                        .result(response)
                        .build());
    }
}
