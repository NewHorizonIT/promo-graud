package group2d.promo_graud.modules.campaigns;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import group2d.promo_graud.modules.campaigns.dto.CampaignRequest;
import group2d.promo_graud.modules.campaigns.dto.CampaignResponse;
import group2d.promo_graud.modules.campaigns.dto.CampaignSearchRequest;
import group2d.promo_graud.modules.campaigns.dto.CampaignUpdateStatusRequest;
import group2d.promo_graud.shared.exception.AppException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Campaign Service Unit Test")
public class CampaignServiceTest {
    @Mock
    // Tạo Repo giả
    private CampaignRepository campaignRepository;

    @InjectMocks private CampaignService campaignService;

    // 1. NHÓM TEST CHO HÀM CREATE CAMPAIGN
    @Nested
    @DisplayName("Tests for createCampaign()")
    class createCampaignTests {
        @Test
        @DisplayName("Tạo chiến dịch thành công")
        void createCampaign_Success() {
            // ARRANGE (chuẩn bị dữ liệu)
            CampaignRequest request = createValidRequest();
            Campaign savedCampaign =
                    createMockCampaign(1, request.getStartTime(), CampaignStatus.UPCOMING);
            when(campaignRepository.save(any(Campaign.class))).thenReturn(savedCampaign);

            // ACT (thực thi)
            CampaignResponse response = campaignService.createCampaign(request);

            // ASSERT (JUNIT kiểm tra kq)
            assertNotNull(response);
            assertEquals("Test Campaign", response.getName());
        }

        @Test
        @DisplayName("Thất bại vì thời gian chiến dịch nằm trong quá khứ")
        void createCampaign_Fail_StartTimeInPast() {
            // Arrange
            CampaignRequest request = createValidRequest();
            // cho startTime ở quá khứ
            request.setStartTime(LocalDateTime.now().minusDays(1)); // Quá khứ

            // Act & Assert
            AppException ex =
                    assertThrows(AppException.class, () -> campaignService.createCampaign(request));
            assertEquals(CampaignErrorCode.CAMPAIGN_IN_PAST, ex.getErrorCode());
            verify(campaignRepository, never()).save(any()); // Đảm bảo không gọi DB
        }

        @Test
        @DisplayName("Thất bại khi Ngân sách âm hoặc bằng 0")
        void createCampaign_Fail_InvalidBudget() {
            // Arrange
            CampaignRequest request = createValidRequest();
            request.setPromotionBudget(BigDecimal.ZERO);

            // Act & Assert
            AppException ex =
                    assertThrows(AppException.class, () -> campaignService.createCampaign(request));
            assertEquals(CampaignErrorCode.INVALID_CAMPAIGN_BUDGET, ex.getErrorCode());
        }

        @Test
        @DisplayName("Thất bại khi EndTime nhỏ hơn hoặc bằng StartTime")
        void createCampaign_Fail_EndTimeBeforeStartTime() {
            CampaignRequest request = createValidRequest();
            request.setEndTime(request.getStartTime().minusDays(1));

            AppException ex =
                    assertThrows(AppException.class, () -> campaignService.createCampaign(request));
            assertEquals(CampaignErrorCode.INVALID_CAMPAIGN_TIME, ex.getErrorCode());
        }

        @Test
        @DisplayName("Thất bại khi Ngân sách là null")
        void createCampaign_Fail_NullBudget() {
            CampaignRequest request = createValidRequest();
            request.setPromotionBudget(null);

            AppException ex =
                    assertThrows(AppException.class, () -> campaignService.createCampaign(request));
            assertEquals(CampaignErrorCode.CAMPAIGN_BUDGET_NOT_NULL, ex.getErrorCode());
        }
    }

    // 2. NHÓM TEST CHO HÀM UPDATE CAMPAIGN
    @Nested
    @DisplayName("Tests for updatedCampaign()")
    class UpdateCampaignTests {

        @Test
        @DisplayName("Cập nhật thành công chiến dịch UPCOMING")
        void updateCampaign_Success() {
            // Arrange
            Integer id = 1;
            CampaignRequest request = createValidRequest();
            Campaign existingCampaign =
                    createMockCampaign(id, request.getStartTime(), CampaignStatus.UPCOMING);

            when(campaignRepository.findById(id)).thenReturn(Optional.of(existingCampaign));
            // THÊM DÒNG NÀY: Mock hành vi save để không bị trả về null
            when(campaignRepository.save(any(Campaign.class))).thenReturn(existingCampaign);
            // Act
            CampaignResponse response = campaignService.updatedCampaign(id, request);

            // Assert
            assertNotNull(response);
            assertEquals(request.getName(), response.getName());
            // Vì Dirty Checking (tự update), hàm ko gọi repository.save() nên ko verify save
        }

        @Test
        @DisplayName("Thất bại khi đổi StartTime của chiến dịch đang ACTIVE")
        void updateCampaign_Fail_ChangeStartTimeWhenActive() {
            // Arrange
            Integer id = 1;
            CampaignRequest request = createValidRequest();
            // StartTime cũ khác với StartTime trong request
            Campaign activeCampaign =
                    createMockCampaign(id, LocalDateTime.now().minusDays(2), CampaignStatus.ACTIVE);

            when(campaignRepository.findById(id)).thenReturn(Optional.of(activeCampaign));

            // Act & Assert
            AppException ex =
                    assertThrows(
                            AppException.class, () -> campaignService.updatedCampaign(id, request));
            assertEquals(CampaignErrorCode.CANNOT_UPDATE_RUNNING_CAMPAIGN, ex.getErrorCode());
        }

        @Test
        @DisplayName("Cập nhật thất bại khi chiến dịch đã bị xóa mềm")
        void updateCampaign_Fail_WhenDeleted() {
            // Arrange
            Integer id = 1;
            Campaign deletedCampaign =
                    createMockCampaign(id, LocalDateTime.now(), CampaignStatus.UPCOMING);
            deletedCampaign.setIsDeleted(true);

            when(campaignRepository.findById(id)).thenReturn(Optional.of(deletedCampaign));

            // Act & Assert
            AppException ex =
                    assertThrows(
                            AppException.class,
                            () -> campaignService.updatedCampaign(id, createValidRequest()));
            assertEquals(CampaignErrorCode.CAMPAIGN_IS_DELETED, ex.getErrorCode());
        }

        @Test
        @DisplayName("Thất bại khi không tìm thấy chiến dịch")
        void updateCampaign_Fail_NotFound() {
            when(campaignRepository.findById(1)).thenReturn(Optional.empty());

            AppException ex =
                    assertThrows(
                            AppException.class,
                            () -> campaignService.updatedCampaign(1, createValidRequest()));
            assertEquals(CampaignErrorCode.CAMPAIGN_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("Thất bại khi cập nhật chiến dịch đã ENDED")
        void updateCampaign_Fail_WhenEnded() {
            Integer id = 1;
            Campaign endedCampaign =
                    createMockCampaign(id, LocalDateTime.now().minusDays(5), CampaignStatus.ENDED);
            when(campaignRepository.findById(id)).thenReturn(Optional.of(endedCampaign));

            AppException ex =
                    assertThrows(
                            AppException.class,
                            () -> campaignService.updatedCampaign(id, createValidRequest()));
            assertEquals(CampaignErrorCode.CAMPAIGN_END, ex.getErrorCode());
        }

        @Test
        @DisplayName("Thất bại khi chiến dịch ACTIVE nhưng set EndTime trong quá khứ")
        void updateCampaign_Fail_ActiveCampaign_EndTimeInPast() {
            Integer id = 1;
            CampaignRequest request = createValidRequest();
            request.setEndTime(LocalDateTime.now().minusDays(1)); // Đặt EndTime ở quá khứ

            // Giữ nguyên StartTime để pass qua check đầu tiên của nhánh ACTIVE
            Campaign activeCampaign =
                    createMockCampaign(id, request.getStartTime(), CampaignStatus.ACTIVE);
            when(campaignRepository.findById(id)).thenReturn(Optional.of(activeCampaign));

            AppException ex =
                    assertThrows(
                            AppException.class, () -> campaignService.updatedCampaign(id, request));
            assertEquals(CampaignErrorCode.END_TIME_MUST_BE_IN_FUTURE, ex.getErrorCode());
        }

        @Test
        @DisplayName("Cập nhật thành công chiến dịch đang ACTIVE (chỉ đổi EndTime)")
        void updateCampaign_Success_ActiveCampaign() {
            Integer id = 1;
            CampaignRequest request = createValidRequest();

            // StartTime trong DB phải khớp với request
            Campaign activeCampaign =
                    createMockCampaign(id, request.getStartTime(), CampaignStatus.ACTIVE);
            when(campaignRepository.findById(id)).thenReturn(Optional.of(activeCampaign));
            when(campaignRepository.save(any(Campaign.class))).thenReturn(activeCampaign);

            CampaignResponse response = campaignService.updatedCampaign(id, request);
            assertNotNull(response);
        }
    }

    // 3. NHÓM TEST CHO HÀM GET ALL
    // 3. NHÓM TEST CHO HÀM GET ALL
    @Nested
    @DisplayName("Tests for getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Lấy danh sách phân trang thành công")
        void getAll_Success() {

            // Arrange
            Campaign mockCampaign =
                    createMockCampaign(1, LocalDateTime.now(), CampaignStatus.ACTIVE);

            Page<Campaign> pagedResponse = new PageImpl<>(List.of(mockCampaign));

            CampaignSearchRequest request = new CampaignSearchRequest();

            request.setName("Test");
            request.setPage(1);
            request.setSize(10);
            request.setSortBy("id");
            request.setSortDir("desc");

            when(campaignRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // Act
            Page<CampaignResponse> result = campaignService.getAll(request);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("Test Campaign", result.getContent().get(0).getName());
        }
    }

    // 4. NHÓM TEST CHO HÀM UPDATE STATUS
    @Nested
    @DisplayName("Tests for updatedStatus()")
    class UpdateStatusTests {
        @Test
        @DisplayName("Cập nhật trạng thái thành công")
        void updateStatus_Success() {
            // Arrange
            Integer id = 1;
            // Đang UPCOMING, StartTime < Now -> Đủ điều kiện sang ACTIVE
            Campaign existingCampaign =
                    createMockCampaign(
                            id, LocalDateTime.now().minusDays(1), CampaignStatus.UPCOMING);
            CampaignUpdateStatusRequest request =
                    new CampaignUpdateStatusRequest(CampaignStatus.ACTIVE);

            when(campaignRepository.findById(id)).thenReturn(Optional.of(existingCampaign));
            when(campaignRepository.save(any(Campaign.class))).thenReturn(existingCampaign);

            // Act
            CampaignResponse response = campaignService.updatedStatus(id, request);

            // Assert
            assertEquals(CampaignStatus.ACTIVE, response.getStatus());
            verify(campaignRepository, times(1)).save(existingCampaign);
        }

        @Test
        @DisplayName("Trả về sớm nếu trạng thái mới giống trạng thái cũ")
        void updateStatus_EarlyReturn_WhenStatusIsSame() {
            // Arrange
            Integer id = 1;
            Campaign existingCampaign =
                    createMockCampaign(id, LocalDateTime.now(), CampaignStatus.ACTIVE);
            CampaignUpdateStatusRequest request =
                    new CampaignUpdateStatusRequest(CampaignStatus.ACTIVE); // Y chang

            when(campaignRepository.findById(id)).thenReturn(Optional.of(existingCampaign));

            // Act
            CampaignResponse response = campaignService.updatedStatus(id, request);

            // Assert
            assertEquals(CampaignStatus.ACTIVE, response.getStatus());
            verify(campaignRepository, never()).save(any()); // Không được gọi DB
        }

        @Test
        @DisplayName("Thất bại khi đổi sang ACTIVE nhưng chưa tới giờ StartTime")
        void updateStatus_Fail_ActivateBeforeStartTime() {
            // Arrange
            Integer id = 1;
            // Start time tận ngày mai
            Campaign existingCampaign =
                    createMockCampaign(
                            id, LocalDateTime.now().plusDays(1), CampaignStatus.UPCOMING);
            CampaignUpdateStatusRequest request =
                    new CampaignUpdateStatusRequest(CampaignStatus.ACTIVE);

            when(campaignRepository.findById(id)).thenReturn(Optional.of(existingCampaign));

            // Act & Assert
            AppException ex =
                    assertThrows(
                            AppException.class, () -> campaignService.updatedStatus(id, request));
            assertEquals(CampaignErrorCode.CANNOT_ACTIVATE_BEFORE_START_TIME, ex.getErrorCode());
        }

        @Test
        @DisplayName("Thất bại khi cập nhật trạng thái của chiến dịch đã ENDED")
        void updateStatus_Fail_WhenEnded() {
            Integer id = 1;
            Campaign endedCampaign =
                    createMockCampaign(id, LocalDateTime.now().minusDays(5), CampaignStatus.ENDED);
            CampaignUpdateStatusRequest request =
                    new CampaignUpdateStatusRequest(CampaignStatus.UPCOMING);

            when(campaignRepository.findById(id)).thenReturn(Optional.of(endedCampaign));

            AppException ex =
                    assertThrows(
                            AppException.class, () -> campaignService.updatedStatus(id, request));
            assertEquals(CampaignErrorCode.CAMPAIGN_END, ex.getErrorCode());
        }
    }

    // 5. NHÓM TEST CHO HÀM DELETE CAMPAIGN
    @Nested
    @DisplayName("Tests for deleteCampaign()")
    class DeleteCampaignTests {
        @Test
        @DisplayName("Xóa mềm thành công khi Campaign đã ENDED")
        void deleteCampaign_Success() {
            // Arrange
            Integer id = 1;
            Campaign existingCampaign =
                    createMockCampaign(id, LocalDateTime.now().minusDays(5), CampaignStatus.ENDED);

            when(campaignRepository.findById(id)).thenReturn(Optional.of(existingCampaign));

            // Act
            campaignService.deleteCampaign(id);

            // Assert
            assertTrue(existingCampaign.getIsDeleted());
            verify(campaignRepository, times(1)).save(existingCampaign);
        }

        @Test
        @DisplayName("Thất bại khi cố xóa Campaign chưa ENDED")
        void deleteCampaign_Fail_WhenNotEnded() {
            // Arrange
            Integer id = 1;
            Campaign existingCampaign =
                    createMockCampaign(id, LocalDateTime.now(), CampaignStatus.ACTIVE);

            when(campaignRepository.findById(id)).thenReturn(Optional.of(existingCampaign));

            // Act & Assert
            AppException ex =
                    assertThrows(AppException.class, () -> campaignService.deleteCampaign(id));
            assertEquals(CampaignErrorCode.CANNOT_DELETE_UNENDED_CAMPAIGN, ex.getErrorCode());
        }
    }

    // 6. NHÓM TEST CHO HÀM CHI TIẾT CAMPAIGN
    @Nested
    @DisplayName("Tests for getCampaignById()")
    class GetCampaignByIdTests {
        @Test
        @DisplayName("Lấy chi tiết chiến dịch thành công")
        void getCampaignById_Success() {
            Integer id = 1;
            Campaign mockCampaign =
                    createMockCampaign(id, LocalDateTime.now(), CampaignStatus.UPCOMING);
            when(campaignRepository.findById(id)).thenReturn(Optional.of(mockCampaign));

            CampaignResponse response = campaignService.getCampaignById(id);

            assertNotNull(response);
            assertEquals(id, response.getId());
        }

        @Test
        @DisplayName("Thất bại khi chiến dịch không tồn tại")
        void getCampaignById_Fail_NotFound() {
            Integer id = 99;
            when(campaignRepository.findById(id)).thenReturn(Optional.empty());

            AppException ex =
                    assertThrows(AppException.class, () -> campaignService.getCampaignById(id));
            assertEquals(CampaignErrorCode.CAMPAIGN_NOT_FOUND, ex.getErrorCode());
        }
    }

    // CÁC HÀM TIỆN ÍCH DÙNG CHUNG
    // Tạo 1 request hợp lệ chuẩn chỉnh
    private CampaignRequest createValidRequest() {
        CampaignRequest request = new CampaignRequest();
        request.setName("Test Campaign");
        request.setStartTime(LocalDateTime.now().plusDays(1)); // Tương lai
        request.setEndTime(LocalDateTime.now().plusDays(5));
        request.setPromotionBudget(new BigDecimal("1000000"));
        return request;
    }

    // Tạo 1 Campaign entity mẫu lấy từ DB
    private Campaign createMockCampaign(
            Integer id, LocalDateTime startTime, CampaignStatus status) {
        return Campaign.builder()
                .id(id)
                .name("Test Campaign")
                .startTime(startTime)
                .endTime(startTime.plusDays(5))
                .promotionBudget(new BigDecimal("1000000"))
                .status(status)
                .isDeleted(false)
                .build();
    }
}
