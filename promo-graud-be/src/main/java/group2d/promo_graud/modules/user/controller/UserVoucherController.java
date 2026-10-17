package group2d.promo_graud.modules.user.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.user.dto.request.UserVoucherRequest;
import group2d.promo_graud.modules.user.dto.response.UserVoucherResponse;
import group2d.promo_graud.modules.user.service.UserVoucherService;
import group2d.promo_graud.shared.dto.ApiResponse;

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
}
