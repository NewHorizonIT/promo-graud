package group2d.promo_graud.modules.rules;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.campaigns.Campaign;
import group2d.promo_graud.modules.campaigns.CampaignRepository;
import group2d.promo_graud.modules.rules.dto.request.RuleCreateRequest;
import group2d.promo_graud.modules.rules.dto.response.RuleResponse;
import group2d.promo_graud.modules.rules.enums.RuleCampaignErrorCode;
import group2d.promo_graud.modules.rules.enums.RuleStatus;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;
import group2d.promo_graud.shared.dto.PaginatedResponse;
import group2d.promo_graud.shared.exception.AppException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RuleCampaignService {
    final RuleCampaignRepository ruleRepository;
    final CampaignRepository campaignRepository;

    // ---------------------- Lấy danh sách ------------------
    public PaginatedResponse<RuleResponse> getAllByFilter(
            String name,
            String typeOfRule,
            String status,
            Integer campaignId,
            int page,
            int pageSize) {

        // Parse String thành Enum, nếu null thì bỏ qua
        TypeOfRule typeEnum =
                (typeOfRule != null) ? TypeOfRule.valueOf(typeOfRule.toUpperCase()) : null;

        RuleStatus statusEnum = (status != null) ? RuleStatus.valueOf(status.toUpperCase()) : null;

        // Phân trang (Client truyền vào từ 1, Spring Data JPA tính từ 0)
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // Gọi query lấy dữ liệu Entity
        Page<RuleCampaign> result =
                ruleRepository.getAllByFilter(name, typeEnum, statusEnum, campaignId, pageable);

        // Map từ Entity sang DTO Response (Khuyên dùng thay vì trả thẳng Entity ra ngoài)
        List<RuleResponse> content =
                result.getContent().stream().map(RuleResponse::fromEntity).toList();

        // Trả về PaginatedResponse giống hệt mẫu của bạn
        return PaginatedResponse.<RuleResponse>builder()
                .data(content)
                .page(page)
                .size(pageSize)
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    // ------------------------ Xóa ---------------------------
    public void deleteRule(Integer id) {
        RuleCampaign rule =
                ruleRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(RuleCampaignErrorCode.RULE_NOT_EXIST));

        rule.setIsDeleted(true);

        ruleRepository.save(rule);
    }

    // ---------------------- Tạo rule ------------------------
    public RuleResponse createRule(RuleCreateRequest request) {
        // 1. Validate logic nội bộ
        validateRuleInternalLogic(request);

        // 2. Fetch Campaign (ném AppException nếu không thấy)
        Campaign campaign =
                campaignRepository
                        .findById(request.getCampaignId())
                        .orElseThrow(
                                () -> new AppException(RuleCampaignErrorCode.CAMPAIGN_NOT_FOUND));

        // 3. Validate thời gian của Rule so với Campaign mẹ
        if (request.getStartTime().isBefore(campaign.getStartTime())
                || request.getEndTime().isAfter(campaign.getEndTime())) {
            throw new AppException(RuleCampaignErrorCode.RULE_TIME_OUTSIDE_CAMPAIGN);
        }

        // 4. Map DTO -> Entity và Lưu DB
        RuleCampaign rule =
                RuleCampaign.builder()
                        .name(request.getName())
                        .campaign(campaign)
                        .typeOfRule(request.getTypeOfRule())
                        .value(request.getValue())
                        .maxDiscountValue(request.getMaxDiscountValue())
                        .minOrderValue(request.getMinOrderValue())
                        .typeOfUser(
                                request.getTypeOfUser() != null
                                        ? request.getTypeOfUser()
                                        : UserTypeEnum.NORMAL)
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .payload(request.getPayload())
                        .status(RuleStatus.ACTIVE)
                        .isDeleted(false)
                        .build();
        RuleCampaign savedRule = ruleRepository.save(rule);
        return RuleResponse.fromEntity(savedRule);
    }

    private void validateRuleInternalLogic(RuleCreateRequest request) {

        // 1. Kiểm tra tính hợp lệ của cặp thời gian
        // Thời gian kết thúc bắt buộc phải diễn ra SAU thời gian bắt đầu (không được trước hoặc
        // bằng)
        if (request.getEndTime().isBefore(request.getStartTime())
                || request.getEndTime().isEqual(request.getStartTime())) {
            throw new AppException(RuleCampaignErrorCode.INVALID_TIME_RANGE);
        }

        // 2. Kiểm tra thời gian bắt đầu so với thực tế
        // Không cho phép tạo một Rule có thời gian bắt đầu nằm ở quá khứ
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new AppException(RuleCampaignErrorCode.START_TIME_IN_PAST);
        }

        // 3. Kiểm tra các ràng buộc logic kinh doanh (Business Logic) dựa theo TỪNG LOẠI khuyến mãi
        switch (request.getTypeOfRule()) {
            case PERCENTAGE:
                // Đối với Giảm theo % (VD: Giảm 10%, 50%)
                // - Giá trị (value) không được null
                // - Phải lớn hơn 0 (không có chuyện giảm 0% hay âm %)
                // - Phải nhỏ hơn hoặc bằng 100 (không thể giảm vượt quá 100% giá trị gốc)
                if (request.getValue() == null
                        || request.getValue().compareTo(BigDecimal.ZERO) <= 0
                        || request.getValue().compareTo(new BigDecimal("100")) > 0) {
                    throw new AppException(RuleCampaignErrorCode.INVALID_PERCENTAGE_VALUE);
                }

                // Cực kỳ quan trọng: Khuyến mãi theo % BẮT BUỘC phải có mức trần giảm tối đa
                // (maxDiscountValue)
                // Ví dụ: Giảm 10% nhưng TỐI ĐA 50.000đ, để tránh thất thoát (khách mua đơn 1 tỷ
                // giảm 10% thì lỗ nặng)
                if (request.getMaxDiscountValue() == null
                        || request.getMaxDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new AppException(RuleCampaignErrorCode.MISSING_MAX_DISCOUNT);
                }
                break;

            case FIXED_AMOUNT:
                // Đối với Giảm số tiền cố định (VD: Giảm thẳng 50.000 VNĐ)
                // - Giá trị tiền được giảm bắt buộc phải > 0
                if (request.getValue() == null
                        || request.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new AppException(RuleCampaignErrorCode.INVALID_FIXED_AMOUNT);
                }

                // Nếu quy tắc có yêu cầu "Giá trị đơn hàng tối thiểu" (minOrderValue) thì
                // Đơn tối thiểu đó phải LỚN HƠN hoặc BẰNG số tiền được giảm.
                // Tránh lỗi ngớ ngẩn: Giảm 50k cho đơn từ 20k -> Khách mua hàng thành ra hệ thống
                // nợ khách 30k.
                if (request.getMinOrderValue() != null
                        && request.getMinOrderValue().compareTo(request.getValue()) < 0) {
                    throw new AppException(RuleCampaignErrorCode.MIN_ORDER_LESS_THAN_DISCOUNT);
                }
                break;

            case TIERED:
            case BUY_X_GET_Y:
                // Đối với các quy tắc phức tạp như Chiết khấu theo bậc thang (TIERED)
                // hoặc Mua X Tặng Y (BUY_X_GET_Y), logic không dùng được các cột value đơn giản.
                // Do đó, BẮT BUỘC phải truyền cấu hình chi tiết dưới dạng JSON vào trường
                // "payload".
                if (request.getPayload() == null || request.getPayload().isEmpty()) {
                    throw new AppException(RuleCampaignErrorCode.MISSING_PAYLOAD);
                }
                break;
        }
    }
}
