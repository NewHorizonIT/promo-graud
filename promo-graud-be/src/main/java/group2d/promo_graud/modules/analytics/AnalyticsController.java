package group2d.promo_graud.modules.analytics;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.analytics.dto.CampaignStatsResponse;
import group2d.promo_graud.modules.analytics.dto.OverviewStatsResponse;
import group2d.promo_graud.modules.analytics.dto.RuleBreakdownResponse;
import group2d.promo_graud.modules.analytics.dto.TimelinePointResponse;
import group2d.promo_graud.modules.analytics.dto.TopUserResponse;
import group2d.promo_graud.modules.analytics.dto.VoucherUtilizationResponse;
import group2d.promo_graud.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyticsController {

    AnalyticsService analyticsService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<OverviewStatsResponse>> getOverview() {
        return ResponseEntity.ok(
                ApiResponse.<OverviewStatsResponse>builder()
                        .code(1000)
                        .message("Lấy thống kê tổng quan thành công")
                        .result(analyticsService.getOverview())
                        .build());
    }

    @GetMapping("/campaigns/{id}/stats")
    public ResponseEntity<ApiResponse<CampaignStatsResponse>> getCampaignStats(
            @PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.<CampaignStatsResponse>builder()
                        .code(1000)
                        .message("Lấy thống kê chiến dịch thành công")
                        .result(analyticsService.getCampaignStats(id))
                        .build());
    }

    @GetMapping("/campaigns/{id}/timeline")
    public ResponseEntity<ApiResponse<List<TimelinePointResponse>>> getTimeline(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime to,
            @RequestParam(defaultValue = "day") String interval) {
        return ResponseEntity.ok(
                ApiResponse.<List<TimelinePointResponse>>builder()
                        .code(1000)
                        .message("Lấy timeline thành công")
                        .result(analyticsService.getTimeline(id, from, to, interval))
                        .build());
    }

    @GetMapping("/campaigns/{id}/rule-breakdown")
    public ResponseEntity<ApiResponse<List<RuleBreakdownResponse>>> getRuleBreakdown(
            @PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.<List<RuleBreakdownResponse>>builder()
                        .code(1000)
                        .message("Lấy rule breakdown thành công")
                        .result(analyticsService.getRuleBreakdown(id))
                        .build());
    }

    @GetMapping("/campaigns/{id}/top-users")
    public ResponseEntity<ApiResponse<List<TopUserResponse>>> getTopUsers(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(
                ApiResponse.<List<TopUserResponse>>builder()
                        .code(1000)
                        .message("Lấy top users thành công")
                        .result(analyticsService.getTopUsers(id, limit))
                        .build());
    }

    @GetMapping("/campaigns/{id}/voucher-utilization")
    public ResponseEntity<ApiResponse<List<VoucherUtilizationResponse>>> getVoucherUtilization(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "usage_rate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(
                ApiResponse.<List<VoucherUtilizationResponse>>builder()
                        .code(1000)
                        .message("Lấy voucher utilization thành công")
                        .result(analyticsService.getVoucherUtilization(id, sortBy, sortDir))
                        .build());
    }
}
