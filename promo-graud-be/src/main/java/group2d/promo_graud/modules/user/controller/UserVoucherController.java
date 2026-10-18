package group2d.promo_graud.modules.user.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.user.dto.request.UserVoucherRequest;
import group2d.promo_graud.modules.user.dto.response.UserVoucherResponse;
import group2d.promo_graud.modules.user.service.UserVoucherService;
import group2d.promo_graud.shared.dto.ApiResponse;
import group2d.promo_graud.shared.dto.PaginatedResponse;

@RestController
@RequestMapping("/api/v1/user-vouchers")
@RequiredArgsConstructor
public class UserVoucherController {
    private final UserVoucherService userVoucherService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserVoucherResponse>> create(
            @Valid @RequestBody UserVoucherRequest request) {
        UserVoucherResponse response = userVoucherService.create(request);
        return ResponseEntity.ok(
                ApiResponse.<UserVoucherResponse>builder()
                        .code(201)
                        .message("Create user_voucher success")
                        .result(response)
                        .build());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserVoucherResponse>>> findAll(
            @Valid @RequestBody UserVoucherRequest request) {
        List<UserVoucherResponse> response = userVoucherService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<UserVoucherResponse>>builder()
                        .code(201)
                        .message("Find all user_voucher success")
                        .result(response)
                        .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserVoucherResponse>>> getMyVoucher(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Integer userId = ((Number) jwt.getClaim("userId")).intValue();

        Page<UserVoucherResponse> userVouchers =
                userVoucherService.getMyVoucher(userId, page, limit);

        PaginatedResponse<UserVoucherResponse> response =
                PaginatedResponse.<UserVoucherResponse>builder()
                        .data(userVouchers.getContent())
                        .page(page)
                        .size(limit)
                        .totalItems(userVouchers.getTotalElements())
                        .totalPages(userVouchers.getTotalPages())
                        .build();

        return ResponseEntity.ok(
                ApiResponse.<PaginatedResponse<UserVoucherResponse>>builder()
                        .code(200)
                        .message("Lấy voucher của user thành công")
                        .result(response)
                        .build());
    }
}
