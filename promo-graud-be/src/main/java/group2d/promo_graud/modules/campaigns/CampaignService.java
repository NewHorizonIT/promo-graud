package group2d.promo_graud.modules.campaigns;

import group2d.promo_graud.modules.campaigns.dto.CampaignRequest;
import group2d.promo_graud.modules.campaigns.dto.CampaignResponse;
import group2d.promo_graud.modules.campaigns.dto.CampaignUpdateStatusRequest;
import group2d.promo_graud.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {
    private final CampaignRepository campaignRepository;

    // Tạo chiến dịch
    @Transactional
    public CampaignResponse createCampaign(CampaignRequest request) {
        validateCampaignTime(request.getStartTime(),request.getEndTime());
        validateCampaignBudget(request.getPromotionBudget());

        Campaign campaign = Campaign.builder()
          .name(request.getName())
          .startTime(request.getStartTime())
          .endTime(request.getEndTime())
          .promotionBudget(request.getPromotionBudget())
          .build();

        Campaign savedCampaign = campaignRepository.save(campaign);

        return mapToCampaignResponse(savedCampaign);
    }

    //  Sửa chiến dịch
    @Transactional
    public CampaignResponse updatedCampaign(Integer id,CampaignRequest request){
        Campaign campaign = findCampaignOrThrow(id);
        LocalDateTime now = LocalDateTime.now();
        // check không cho sửa khi chiến dịch kết thúc
        if(campaign.getStatus() == CampaignStatus.ENDED) {
            throw new AppException(CampaignErrorCode.CAMPAIGN_END);
        }
        // check không cho sửa thời gian bắt đầu khi chiến dịch đang diễn ra
        if(campaign.getStatus() == CampaignStatus.ACTIVE) {
            if(!campaign.getStartTime().equals(request.getStartTime())) {
                throw new AppException(CampaignErrorCode.CANNOT_UPDATE_RUNNING_CAMPAIGN);
            }
            // endTime mới bắt buộc phải ở trong tương lai (lớn hơn hiện tại)
            if (request.getEndTime().isBefore(now)) {
                throw new AppException(CampaignErrorCode.END_TIME_MUST_BE_IN_FUTURE);
            }
        }

        // Logic cập nhật khi chiến dịch CHƯA DIỄN RA (UPCOMING)
        else if (campaign.getStatus() == CampaignStatus.UPCOMING) {
          // Hàm validate startTime > now và endTime > startTime
          validateCampaignTime(request.getStartTime(), request.getEndTime());
        }
        validateCampaignBudget(request.getPromotionBudget());

        campaign.setName(request.getName());
        campaign.setStartTime(request.getStartTime());
        campaign.setEndTime(request.getEndTime());
        campaign.setPromotionBudget(request.getPromotionBudget());
        Campaign savedCampaign = campaignRepository.save(campaign);
        return mapToCampaignResponse(savedCampaign);
    }

    // lấy danh sách chiến dịch
    @Transactional(readOnly = true)
    public Page<CampaignResponse>getAll(String name,CampaignStatus status
                                      ,LocalDateTime startTime,LocalDateTime endTime
                                      ,int page,int size
                                      ,String sortBy,String sortDir) {
        Sort sort = buildSortCampaign(sortBy,sortDir);
        //Tạo cấu hình Phân trang (Pageable)
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        // Bắt buộc nhập cả 2, hoặc không nhập gì cả
        if ((startTime != null && endTime == null) || (startTime == null && endTime != null)) {
            throw new AppException(CampaignErrorCode.INVALID_TIME_FILTER);
        }

        // Chỉ check khi cả 2 biến đều có giá trị (không bị null)
        if (startTime != null && endTime != null) {
            if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
                throw new AppException(CampaignErrorCode.END_TIME_MUST_BE_AFTER_START_TIME);
            }
        }

        // Chuẩn hóa name để tránh PostgreSQL/Hibernate
        // không xác định được kiểu dữ liệu khi name = null
        String normalizedName = name == null ? "" : name.trim();

        // Tạo các biến cờ (Nếu khác null nghĩa là có giá trị -> true)
        boolean hasStatus = (status != null);
        boolean hasStartTime = (startTime != null);
        boolean hasEndTime = (endTime != null);
        Page<Campaign> campaignPage = campaignRepository.searchAndFilterCampaign(
            normalizedName,
            status,hasStatus,
            startTime, hasStartTime,
            endTime, hasEndTime,
            pageable
        );

        return campaignPage.map(this::mapToCampaignResponse);
    }

    // Cập nhật status của Campaign
    @Transactional
    public CampaignResponse updatedStatus(Integer id,CampaignUpdateStatusRequest request) {
        Campaign campaign = findCampaignOrThrow(id);
        CampaignStatus newStatus = request.getStatus();
        CampaignStatus currentStatus = campaign.getStatus();
        LocalDateTime now = LocalDateTime.now();

        // 1.Trùng trạng thái hiện tại -> Trả về luôn không cần update DB
        if(newStatus == currentStatus) {
            return mapToCampaignResponse(campaign);
        }
        // 2.Campaign đã END -> không cho update
        if(currentStatus == CampaignStatus.ENDED){
            throw new AppException(CampaignErrorCode.CAMPAIGN_END);
        }
        // 3.Nếu muốn đổi sang ACTIVE -> startTime <= now
        if(newStatus == CampaignStatus.ACTIVE) {
            if(campaign.getStartTime().isAfter(now)){
                throw new AppException(CampaignErrorCode.CANNOT_ACTIVATE_BEFORE_START_TIME);
            }
        }
        campaign.setStatus(newStatus);
        Campaign saveStatus = campaignRepository.save(campaign);
        return mapToCampaignResponse(saveStatus);
    }

    // Xóa chiến dịch
    @Transactional
    public void deleteCampaign(Integer id) {
        Campaign campaign = findCampaignOrThrow(id);

        if (campaign.getStatus() != CampaignStatus.ENDED) {
          throw new AppException(CampaignErrorCode.CANNOT_DELETE_UNENDED_CAMPAIGN);
        }

        campaign.setIsDeleted(true);

        campaignRepository.save(campaign);
    }

    // lấy chi tiết 1 chiến dịch
    public CampaignResponse getCampaignById(Integer id) {
        Campaign campaign = findCampaignOrThrow(id);
        return mapToCampaignResponse(campaign);
    }
    //----------------------Hàm nội bộ---------------------------//
    private CampaignResponse mapToCampaignResponse(Campaign campaign) {

        return CampaignResponse.builder()
          .id(campaign.getId())
          .name(campaign.getName())
          .startTime(campaign.getStartTime())
          .endTime(campaign.getEndTime())
          .promotionBudget(campaign.getPromotionBudget())
          .status(campaign.getStatus())
          .createdAt(campaign.getCreatedAt())
          .updatedAt(campaign.getUpdatedAt())
          .isDeleted(campaign.getIsDeleted())
          .build();
    }

    // Hàm kiểm tra thời gian
    private void validateCampaignTime(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();

        if(startTime.isBefore(now)) {
          throw new AppException(CampaignErrorCode.CAMPAIGN_IN_PAST);
        }
        if(startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
          throw new AppException(CampaignErrorCode.INVALID_CAMPAIGN_TIME);
        }
    }

    // Hàm kiểm tra ngân sách
    private void validateCampaignBudget(BigDecimal promotionBudget) {
        if(promotionBudget == null) {
          throw new AppException(CampaignErrorCode.CAMPAIGN_BUDGET_NOT_NULL);
        }
        if(promotionBudget.compareTo(BigDecimal.ZERO) <= 0) {
          throw new AppException(CampaignErrorCode.INVALID_CAMPAIGN_BUDGET);
        }
    }

    // Hàm kiểm tra chiến dịch có tồn tại
    private Campaign findCampaignOrThrow(Integer id) {
        Campaign campaign = campaignRepository.findById(id)
          .orElseThrow(() -> new AppException(CampaignErrorCode.CAMPAIGN_NOT_FOUND));
        if(Boolean.TRUE.equals(campaign.getIsDeleted())) {
          throw new AppException(CampaignErrorCode.CAMPAIGN_IS_DELETED);
        }
        return campaign;
    }

    // Hàm sắp xếp của campaign
    private Sort buildSortCampaign(String sortBy,String sortDir) {
        // nếu người dùng sort tăng dần
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // nếu sort theo budget
        if("promotionBudget".equalsIgnoreCase(sortBy)) {
            return Sort.by(direction, "promotionBudget");
        }

        // Mặc định: Luôn giảm dần theo ID (mới nhất lên đầu)
        return Sort.by(Sort.Direction.DESC, "id");
    }
}
