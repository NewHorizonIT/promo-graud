package group2d.promo_graud.modules.campaigns;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.campaigns.dto.CampaignRequest;
import group2d.promo_graud.modules.campaigns.dto.CampaignResponse;
import group2d.promo_graud.modules.campaigns.dto.CampaignSearchRequest;
import group2d.promo_graud.modules.campaigns.dto.CampaignUpdateStatusRequest;
import group2d.promo_graud.shared.dto.*;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    // tạo chiến dịch
    @PostMapping
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(
            @Valid @RequestBody CampaignRequest request) {

        CampaignResponse response = campaignService.createCampaign(request);

        return ResponseEntity.ok(
                ApiResponse.<CampaignResponse>builder()
                        .code(1000)
                        .message("Tạo campaign thành công")
                        .result(response)
                        .build());
    }

    // sửa chiến dịch
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> updatedCampaign(
            @PathVariable Integer id, @Valid @RequestBody CampaignRequest request) {

        CampaignResponse response = campaignService.updatedCampaign(id, request);

        return ResponseEntity.ok(
                ApiResponse.<CampaignResponse>builder()
                        .code(1000)
                        .message("Sửa thông tin campaign thành công")
                        .result(response)
                        .build());
    }

    // Cập nhật trạng thái chiến dịch
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> updatedStatus(
            @PathVariable Integer id, @Valid @RequestBody CampaignUpdateStatusRequest request) {

        CampaignResponse response = campaignService.updatedStatus(id, request);

        return ResponseEntity.ok(
                ApiResponse.<CampaignResponse>builder()
                        .code(1000)
                        .message("Cập nhật status thành công")
                        .result(response)
                        .build());
    }

    // lấy danh sách chiến dịch có phân trang
    @GetMapping
    public ResponseEntity<PaginatedResponse<CampaignResponse>> getAllAndFilter(
            @ModelAttribute CampaignSearchRequest request) {
        Page<CampaignResponse> result = campaignService.getAll(request);

        PaginatedResponse<CampaignResponse> response =
                PaginatedResponse.<CampaignResponse>builder()
                        .data(result.getContent())
                        .page(result.getNumber() + 1)
                        .size(result.getSize())
                        .totalItems(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .build();

        return ResponseEntity.ok(response);
    }

    // xóa chiến dịch
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable Integer id) {

        campaignService.deleteCampaign(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(1000)
                        .message("Xóa thành công chiến dịch")
                        .build());
    }

    // lấy chi tiết chiến dịch
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaignById(@PathVariable Integer id) {
        CampaignResponse response = campaignService.getCampaignById(id);
        return ResponseEntity.ok(
                ApiResponse.<CampaignResponse>builder()
                        .code(1000)
                        .message("Lấy chi tiết thông tin chiến dịch thành công")
                        .result(response)
                        .build());
    }
}
