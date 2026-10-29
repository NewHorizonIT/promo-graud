package group2d.promo_graud.modules.campaigns;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import group2d.promo_graud.modules.campaigns.dto.CampaignRequest;
import group2d.promo_graud.modules.campaigns.dto.CampaignResponse;
import group2d.promo_graud.modules.campaigns.dto.CampaignSearchRequest;
import group2d.promo_graud.modules.campaigns.dto.CampaignUpdateStatusRequest;
import group2d.promo_graud.modules.campaigns.service.CampaignService;

@WebMvcTest(CampaignController.class)
// Tắt Spring Security filter chain: test này chỉ kiểm tra controller (routing, binding, validation,
// JSON).
// Nếu không tắt, GET sẽ bị 401 (chưa đăng nhập) và POST/PUT/PATCH/DELETE bị 403 (thiếu CSRF token).
@AutoConfigureMockMvc(addFilters = false)
public class CampaignControllerTest {

    private static final String BASE_URL = "/api/v1/campaigns";

    @Autowired private MockMvc mockMvc;

    @MockitoBean private CampaignService campaignService;

    private ObjectMapper objectMapper;

    private CampaignResponse mockResponse;

    @BeforeEach
    void setUp() {
        // Tự khởi tạo và đăng ký module hỗ trợ LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockResponse =
                CampaignResponse.builder()
                        .id(1)
                        .name("Siêu Sale 11/11")
                        .promotionBudget(new BigDecimal("5000000"))
                        .status(CampaignStatus.UPCOMING)
                        .startTime(LocalDateTime.now().plusDays(1))
                        .endTime(LocalDateTime.now().plusDays(5))
                        .build();
    }

    // ======================= HELPER =======================

    private CampaignRequest validCampaignRequest() {
        CampaignRequest request = new CampaignRequest();
        request.setName("Siêu Sale 11/11");
        request.setPromotionBudget(new BigDecimal("5000000"));
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(5));
        return request;
    }

    /** Serialize request hợp lệ rồi bỏ đi 1 field để giả lập client gửi thiếu dữ liệu. */
    private String jsonWithout(Object request, String field) {
        ObjectNode node = objectMapper.valueToTree(request);
        node.remove(field);
        return node.toString();
    }

    // ======================= HAPPY PATH =======================

    @Test
    @DisplayName("Tạo chiến dịch thành công trả về HTTP 200")
    void test_CreateCampaign_Success() throws Exception {
        CampaignRequest request = validCampaignRequest();

        when(campaignService.createCampaign(any(CampaignRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tạo campaign thành công"))
                .andExpect(jsonPath("$.result.id").value(1))
                .andExpect(jsonPath("$.result.name").value("Siêu Sale 11/11"));

        // Kiểm tra JSON đã được bind đúng vào DTO trước khi gọi service
        ArgumentCaptor<CampaignRequest> captor = ArgumentCaptor.forClass(CampaignRequest.class);
        verify(campaignService).createCampaign(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Siêu Sale 11/11");
        assertThat(captor.getValue().getPromotionBudget()).isEqualByComparingTo("5000000");
    }

    @Test
    @DisplayName("Sửa thông tin chiến dịch thành công")
    void test_UpdateCampaign_Success() throws Exception {
        CampaignRequest request = validCampaignRequest();

        when(campaignService.updatedCampaign(eq(1), any(CampaignRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(
                        put(BASE_URL + "/{id}", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Sửa thông tin campaign thành công"))
                .andExpect(jsonPath("$.result.id").value(1));

        verify(campaignService).updatedCampaign(eq(1), any(CampaignRequest.class));
    }

    @Test
    @DisplayName("Cập nhật trạng thái chiến dịch thành công")
    void test_UpdateStatus_Success() throws Exception {
        CampaignUpdateStatusRequest request = new CampaignUpdateStatusRequest();
        request.setStatus(CampaignStatus.ACTIVE);

        mockResponse.setStatus(CampaignStatus.ACTIVE);

        when(campaignService.updatedStatus(eq(1), any(CampaignUpdateStatusRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(
                        patch(BASE_URL + "/{id}", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật status thành công"))
                .andExpect(jsonPath("$.result.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Lấy danh sách chiến dịch có phân trang")
    void test_GetAllAndFilter_Success() throws Exception {
        Page<CampaignResponse> mockPage =
                new PageImpl<>(List.of(mockResponse), PageRequest.of(0, 10), 1);

        when(campaignService.getAll(any(CampaignSearchRequest.class))).thenReturn(mockPage);

        mockMvc.perform(get(BASE_URL).param("page", "1").param("size", "10").param("name", "Sale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Siêu Sale 11/11"));

        // Kiểm tra query param đã được bind vào CampaignSearchRequest
        ArgumentCaptor<CampaignSearchRequest> captor =
                ArgumentCaptor.forClass(CampaignSearchRequest.class);
        verify(campaignService).getAll(captor.capture());
        assertThat(captor.getValue().getPage()).isEqualTo(1);
        assertThat(captor.getValue().getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Lấy chi tiết chiến dịch theo ID")
    void test_GetCampaignById_Success() throws Exception {
        when(campaignService.getCampaignById(1)).thenReturn(mockResponse);

        mockMvc.perform(get(BASE_URL + "/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.id").value(1));
    }

    @Test
    @DisplayName("Xóa chiến dịch thành công")
    void test_DeleteCampaign_Success() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Xóa thành công chiến dịch"));

        verify(campaignService).deleteCampaign(1);
    }

    // ======================= VALIDATION (400) =======================

    @ParameterizedTest(name = "POST thiếu field ''{0}'' -> 400")
    @ValueSource(strings = {"name", "startTime", "endTime", "promotionBudget"})
    @DisplayName("Tạo chiến dịch thiếu field bắt buộc trả về HTTP 400")
    void test_CreateCampaign_MissingField_Returns400(String missingField) throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonWithout(validCampaignRequest(), missingField)))
                .andExpect(status().isBadRequest());

        // Validation phải chặn ngay ở controller, service không được gọi
        verifyNoInteractions(campaignService);
    }

    @Test
    @DisplayName("Tạo chiến dịch với body rỗng {} trả về HTTP 400")
    void test_CreateCampaign_EmptyJson_Returns400() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(campaignService);
    }

    @ParameterizedTest(name = "PUT thiếu field ''{0}'' -> 400")
    @ValueSource(strings = {"name", "startTime", "endTime", "promotionBudget"})
    @DisplayName("Sửa chiến dịch thiếu field bắt buộc trả về HTTP 400")
    void test_UpdateCampaign_MissingField_Returns400(String missingField) throws Exception {
        mockMvc.perform(
                        put(BASE_URL + "/{id}", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonWithout(validCampaignRequest(), missingField)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(campaignService);
    }

    @Test
    @DisplayName("Cập nhật status thiếu field status trả về HTTP 400")
    void test_UpdateStatus_MissingStatus_Returns400() throws Exception {
        mockMvc.perform(
                        patch(BASE_URL + "/{id}", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(campaignService);
    }
}
