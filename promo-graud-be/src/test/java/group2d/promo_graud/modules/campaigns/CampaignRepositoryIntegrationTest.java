package group2d.promo_graud.modules.campaigns;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

import group2d.promo_graud.modules.campaigns.dto.CampaignSearchRequest;
import group2d.promo_graud.modules.campaigns.specification.CampaignSpecification;

@DataJpaTest
@TestPropertySource(
        properties = {
            "spring.flyway.enabled=false",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
            "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
        })
public class CampaignRepositoryIntegrationTest {

    @Autowired private CampaignRepository campaignRepository;

    private Campaign upcomingCampaign;
    private Campaign activeButExpiredCampaign;
    private Campaign deletedCampaign;

    @BeforeEach
    void setUp() {
        // Clear DB trước mỗi lần test (an toàn)
        campaignRepository.deleteAll();
        LocalDateTime now = LocalDateTime.now();

        // 1. Chiến dịch sẽ diễn ra vào 1 tiếng nữa
        upcomingCampaign =
                Campaign.builder()
                        .name("Flash Sale Sinh Viên")
                        .startTime(now.plusHours(1))
                        .endTime(now.plusHours(5))
                        .promotionBudget(new BigDecimal("1000000"))
                        .status(CampaignStatus.UPCOMING)
                        .build();

        // 2. Chiến dịch đang chạy nhưng thực tế thời gian đã qua
        activeButExpiredCampaign =
                Campaign.builder()
                        .name("Trung Thu 2023")
                        .startTime(now.minusDays(5))
                        .endTime(now.minusDays(1))
                        .promotionBudget(new BigDecimal("500000"))
                        .status(CampaignStatus.ACTIVE)
                        .build();

        // 3. Chiến dịch đã bị xóa mềm
        deletedCampaign =
                Campaign.builder()
                        .name("Chiến dịch nháp đã xóa")
                        .startTime(now.plusDays(1))
                        .endTime(now.plusDays(2))
                        .promotionBudget(new BigDecimal("100000"))
                        .status(CampaignStatus.UPCOMING)
                        .isDeleted(true) // Đã xóa
                        .build();

        campaignRepository.saveAll(
                List.of(upcomingCampaign, activeButExpiredCampaign, deletedCampaign));
    }

    // ==========================================
    // NHÓM 1: TEST BACKGROUND JOB (ACTIVATE / END STRICT)
    // ==========================================

    @Test
    @DisplayName("Kích hoạt thành công khi Job chạy đúng giờ hoặc trễ hơn startTime")
    void test_ActivateByIdStrict_Success() {
        // Giả lập thời gian hệ thống là thời điểm 2 tiếng sau (đã qua startTime)
        LocalDateTime mockNow = LocalDateTime.now().plusHours(2);

        int updatedRows =
                campaignRepository.activateByIdStrict(
                        upcomingCampaign.getId(),
                        CampaignStatus.UPCOMING,
                        CampaignStatus.ACTIVE,
                        mockNow);

        assertThat(updatedRows).isEqualTo(1); // Phải update được 1 dòng

        Campaign fromDb = campaignRepository.findById(upcomingCampaign.getId()).get();
        assertThat(fromDb.getStatus()).isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    @DisplayName("Chặn Job rác (Job cũ) chạy trước khi startTime bắt đầu")
    void test_ActivateByIdStrict_Fail_WhenJobFiresTooEarly() {
        // Giả lập thời gian hệ thống mới chỉ trôi qua 30 phút (Chưa tới startTime là 1 tiếng nữa)
        LocalDateTime mockNow = LocalDateTime.now().plusMinutes(30);

        int updatedRows =
                campaignRepository.activateByIdStrict(
                        upcomingCampaign.getId(),
                        CampaignStatus.UPCOMING,
                        CampaignStatus.ACTIVE,
                        mockNow);

        assertThat(updatedRows).isEqualTo(0); // Không dòng nào được update

        Campaign fromDb = campaignRepository.findById(upcomingCampaign.getId()).get();
        assertThat(fromDb.getStatus()).isEqualTo(CampaignStatus.UPCOMING); // Trạng thái giữ nguyên
    }

    // ==========================================
    // NHÓM 2: TEST CRONJOB (VÉT LƯỚI QUÉT HÀNG LOẠT)
    // ==========================================

    @Test
    @DisplayName("Cronjob quét và tự động chuyển trạng thái các chiến dịch hết hạn hoặc tới giờ")
    void test_Cronjob_Scan() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Quét đóng chiến dịch hết hạn
        int endedCount =
                campaignRepository.updateStatusForExpired(
                        CampaignStatus.ACTIVE, CampaignStatus.ENDED, now);
        // 2. Quét mở chiến dịch tới giờ
        // (Trong case này mình tạo upcomingCampaign có startTime là 1h ở tương lai, nên dòng dưới
        // này quét now() sẽ ko thấy cái nào)
        int activatedCount =
                campaignRepository.activateUpcoming(
                        CampaignStatus.UPCOMING, CampaignStatus.ACTIVE, now);

        assertThat(endedCount).isEqualTo(1); // Chiến dịch "Trung Thu 2023" đã bị chuyển thành ENDED
        assertThat(activatedCount).isEqualTo(0); // Chưa có cái nào tới giờ để thành ACTIVE
    }

    // ==========================================
    // NHÓM 3: TEST BỘ LỌC (SPECIFICATION)
    // ==========================================

    @Test
    @DisplayName("Specification luôn bỏ qua các chiến dịch đã bị xóa mềm (isDeleted = true)")
    void test_Specification_Filter_NotReturnDeleted() {
        CampaignSearchRequest request = new CampaignSearchRequest(); // Request rỗng (Lấy tất cả)

        Specification<Campaign> spec = CampaignSpecification.filter(request);
        Page<Campaign> result = campaignRepository.findAll(spec, PageRequest.of(0, 10));

        // DB có 3 campaign, 1 cái bị xóa mềm. Nên kết quả tìm kiếm chỉ được trả về 2.
        assertThat(result.getTotalElements()).isEqualTo(2);

        // Đảm bảo không có thằng nào trong list trả về bị isDeleted = true
        boolean hasDeleted = result.getContent().stream().anyMatch(Campaign::getIsDeleted);
        assertThat(hasDeleted).isFalse();
    }

    @Test
    @DisplayName("Tìm kiếm chiến dịch theo chuỗi tên chứa từ khóa")
    void test_Specification_Filter_ByName() {
        CampaignSearchRequest request = new CampaignSearchRequest();
        request.setName("sinh viên"); // Tìm từ khóa viết thường, 1 phần của chuỗi

        Specification<Campaign> spec = CampaignSpecification.filter(request);
        List<Campaign> result = campaignRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Flash Sale Sinh Viên");
    }
}
