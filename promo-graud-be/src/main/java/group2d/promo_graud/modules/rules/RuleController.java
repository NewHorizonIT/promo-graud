package group2d.promo_graud.modules.rules;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.rules.dto.request.RuleCreateRequest;
import group2d.promo_graud.modules.rules.dto.response.RuleResponse;
import group2d.promo_graud.shared.dto.ApiResponse;
import group2d.promo_graud.shared.dto.PaginatedResponse;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {
    private final RuleCampaignService ruleCampaignService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<RuleResponse>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String typeOfRule,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer campaignId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        PaginatedResponse<RuleResponse> response =
                ruleCampaignService.getAllByFilter(
                        name, typeOfRule, status, campaignId, page, pageSize);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RuleResponse>> createRule(
            @Valid @RequestBody RuleCreateRequest request) {
        RuleResponse response = ruleCampaignService.createRule(request);

        return ResponseEntity.ok(
                ApiResponse.<RuleResponse>builder()
                        .code(1000)
                        .message("Tạo rule thành công")
                        .result(response)
                        .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRules(@PathVariable("id") Integer id) {
        deleteRules(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder().code(1000).message("Xóa voucher thành công ").build());
    }
}
