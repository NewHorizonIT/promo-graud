package group2d.promo_graud.vouchers.unit_test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.RuleCampaignErrorCode;
import group2d.promo_graud.modules.rules.RuleCampaignRepository;
import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.VoucherInsertHelper;
import group2d.promo_graud.modules.voucher.VoucherRepository;
import group2d.promo_graud.modules.voucher.VoucherService;
import group2d.promo_graud.modules.voucher.dto.requests.UpdatedVoucherRequest;
import group2d.promo_graud.modules.voucher.dto.requests.VoucherRequest;
import group2d.promo_graud.modules.voucher.dto.responses.CreateVoucherResponse;
import group2d.promo_graud.modules.voucher.dto.responses.VoucherResponse;
import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.VoucherErrorCode;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;
import group2d.promo_graud.shared.dto.PaginatedResponse;
import group2d.promo_graud.shared.exception.AppException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Voucher Service Unit Test")
public class VoucherServiceUnitTest {

    // Tạo Repo giả
    @Mock private VoucherRepository voucherRepository;

    @Mock private RuleCampaignRepository ruleCampaignRepository;

    @Mock private VoucherInsertHelper voucherInsertHelper;

    @InjectMocks private VoucherService voucherService;

    // 1. NHÓM TEST CHO HÀM GET ALL
    @Nested
    @DisplayName("Tests for getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Lấy danh sách phân trang thành công")
        void getAll_Success() {
            // Arrange
            Voucher mockVoucher =
                    createMockVoucher(1, VoucherType.GENERIC, VoucherStatus.ACTIVE, 100, 100);
            Page<Voucher> pagedResponse =
                    new PageImpl<>(List.of(mockVoucher), PageRequest.of(0, 5), 1);

            when(voucherRepository.getAll(
                            eq(VoucherType.GENERIC),
                            eq(VoucherStatus.ACTIVE),
                            eq(10),
                            eq(DistributionChannel.WEBHOOK),
                            any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // Act (truyền chữ thường để test luôn việc toUpperCase())
            PaginatedResponse<Voucher> result =
                    voucherService.getAll("generic", "active", 10, "webhook", 1, 5);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(1, result.getPage());
            assertEquals(5, result.getSize());
            assertEquals(1L, result.getTotalItems());
            assertEquals(1, result.getTotalPages());

            // page truyền vào là 1-based, xuống repository phải là 0-based
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(voucherRepository).getAll(any(), any(), any(), any(), captor.capture());
            assertEquals(0, captor.getValue().getPageNumber());
            assertEquals(5, captor.getValue().getPageSize());
        }

        @Test
        @DisplayName("Các filter null thì truyền null xuống repository")
        void getAll_Success_NullFilters() {
            // Arrange
            when(voucherRepository.getAll(
                            isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // Act
            PaginatedResponse<Voucher> result =
                    voucherService.getAll(null, null, null, null, 1, 10);

            // Assert
            assertNotNull(result);
            assertTrue(result.getData().isEmpty());
        }

        @Test
        @DisplayName("Thất bại khi type không hợp lệ")
        void getAll_Fail_InvalidType() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> voucherService.getAll("abc", null, null, null, 1, 10));
            verifyNoInteractions(voucherRepository);
        }

        @Test
        @DisplayName("Thất bại khi status không hợp lệ")
        void getAll_Fail_InvalidStatus() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> voucherService.getAll(null, "abc", null, null, 1, 10));
            verifyNoInteractions(voucherRepository);
        }

        @Test
        @DisplayName("Thất bại khi distributionChannel không hợp lệ")
        void getAll_Fail_InvalidDistributionChannel() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> voucherService.getAll(null, null, null, "abc", 1, 10));
            verifyNoInteractions(voucherRepository);
        }
    }

    // 2. NHÓM TEST CHO HÀM UPDATE VOUCHER
    @Nested
    @DisplayName("Tests for update()")
    class UpdateVoucherTests {

        @Test
        @DisplayName("Thất bại khi không truyền status và quantityRemain")
        void update_Fail_EmptyRequest() {
            // Arrange
            UpdatedVoucherRequest request = new UpdatedVoucherRequest();

            // Act & Assert
            AppException ex =
                    assertThrows(AppException.class, () -> voucherService.update(1, request));
            assertEquals(VoucherErrorCode.REQUIRED_FIELD, ex.getErrorCode());
            verifyNoInteractions(voucherRepository); // Chưa cần chạm DB
        }

        @Test
        @DisplayName("Thất bại khi không tìm thấy voucher")
        void update_Fail_NotFound() {
            // Arrange
            UpdatedVoucherRequest request = createUpdateRequest(null, 5);
            when(voucherRepository.findById(99)).thenReturn(Optional.empty());

            // Act & Assert
            AppException ex =
                    assertThrows(AppException.class, () -> voucherService.update(99, request));
            assertEquals(VoucherErrorCode.NOT_EXIST, ex.getErrorCode());
            verify(voucherRepository, never()).save(any());
        }

        @Test
        @DisplayName("Thất bại khi sửa quantityRemain của voucher UNIQUE")
        void update_Fail_UniqueVoucher_UpdateQuantityRemain() {
            // Arrange
            Integer id = 1;
            Voucher uniqueVoucher =
                    createMockVoucher(id, VoucherType.UNIQUE, VoucherStatus.ACTIVE, 1, 1);
            UpdatedVoucherRequest request = createUpdateRequest(null, 0);
            when(voucherRepository.findById(id)).thenReturn(Optional.of(uniqueVoucher));

            // Act & Assert
            AppException ex =
                    assertThrows(AppException.class, () -> voucherService.update(id, request));
            assertEquals(VoucherErrorCode.GENERIC_QUANTITY_NOT_EDITABLE, ex.getErrorCode());
            assertEquals(1, uniqueVoucher.getQuantityRemain()); // Không bị đổi
            verify(voucherRepository, never()).save(any());
        }

        @Test
        @DisplayName("Thất bại khi voucher GENERIC có quantityRemain lớn hơn quantity")
        void update_Fail_Generic_QuantityRemainExceedQuantity() {
            // Arrange
            Integer id = 1;
            Voucher genericVoucher =
                    createMockVoucher(id, VoucherType.GENERIC, VoucherStatus.ACTIVE, 100, 50);
            UpdatedVoucherRequest request = createUpdateRequest(null, 101);
            when(voucherRepository.findById(id)).thenReturn(Optional.of(genericVoucher));

            // Act & Assert
            AppException ex =
                    assertThrows(AppException.class, () -> voucherService.update(id, request));
            assertEquals(VoucherErrorCode.INSUFFICIENT_QUANTITY, ex.getErrorCode());
            assertEquals(50, genericVoucher.getQuantityRemain()); // Không bị đổi
            verify(voucherRepository, never()).save(any());
        }

        @Test
        @DisplayName("Thất bại khi đổi trạng thái sang ACTIVE")
        void update_Fail_StatusActive() {
            // Arrange
            Integer id = 1;
            Voucher voucher = createMockVoucher(id, VoucherType.UNIQUE, anyNonActiveStatus(), 1, 1);
            UpdatedVoucherRequest request = createUpdateRequest(VoucherStatus.ACTIVE, null);
            when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));

            // Act & Assert
            AppException ex =
                    assertThrows(AppException.class, () -> voucherService.update(id, request));
            assertEquals(VoucherErrorCode.INVALID_STATUS, ex.getErrorCode());
            verify(voucherRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cập nhật quantityRemain thành công cho voucher GENERIC")
        void update_Success_Generic_QuantityRemain() {
            // Arrange
            Integer id = 1;
            Voucher genericVoucher =
                    createMockVoucher(id, VoucherType.GENERIC, VoucherStatus.ACTIVE, 100, 100);
            UpdatedVoucherRequest request = createUpdateRequest(null, 60);
            when(voucherRepository.findById(id)).thenReturn(Optional.of(genericVoucher));

            // Act
            VoucherResponse response = voucherService.update(id, request);

            // Assert
            assertNotNull(response);
            assertEquals(60, response.getQuantityRemain());
            assertEquals(100, response.getQuantity());
            assertEquals(10, response.getRuleId());
            assertEquals("TET2026", response.getCode());
            assertEquals(VoucherStatus.ACTIVE.name(), response.getStatus());
            verify(voucherRepository, times(1)).save(genericVoucher);
        }

        @Test
        @DisplayName("Cập nhật thành công khi quantityRemain bằng đúng quantity (biên)")
        void update_Success_Generic_QuantityRemainEqualQuantity() {
            // Arrange
            Integer id = 1;
            Voucher genericVoucher =
                    createMockVoucher(id, VoucherType.GENERIC, VoucherStatus.ACTIVE, 100, 10);
            UpdatedVoucherRequest request = createUpdateRequest(null, 100);
            when(voucherRepository.findById(id)).thenReturn(Optional.of(genericVoucher));

            // Act
            VoucherResponse response = voucherService.update(id, request);

            // Assert
            assertEquals(100, response.getQuantityRemain());
            verify(voucherRepository, times(1)).save(genericVoucher);
        }

        @Test
        @DisplayName("Cập nhật status thành công cho voucher UNIQUE")
        void update_Success_Unique_StatusOnly() {
            // Arrange
            Integer id = 1;
            VoucherStatus newStatus = anyNonActiveStatus();
            Voucher uniqueVoucher =
                    createMockVoucher(id, VoucherType.UNIQUE, VoucherStatus.ACTIVE, 1, 1);
            UpdatedVoucherRequest request = createUpdateRequest(newStatus, null);
            when(voucherRepository.findById(id)).thenReturn(Optional.of(uniqueVoucher));

            // Act
            VoucherResponse response = voucherService.update(id, request);

            // Assert
            assertEquals(newStatus.name(), response.getStatus());
            assertEquals(1, response.getQuantityRemain());
            verify(voucherRepository, times(1)).save(uniqueVoucher);
        }

        // LƯU Ý: test này sẽ FAIL (NullPointerException) cho tới khi sửa service.
        // Nguyên nhân: request.getQuantityRemain() > voucher.getQuantity() bị unboxing null
        // khi voucher GENERIC chỉ gửi status. Cần thêm điều kiện quantityRemain != null.
        @Test
        @DisplayName("Cập nhật status thành công cho voucher GENERIC (không gửi quantityRemain)")
        void update_Success_Generic_StatusOnly() {
            // Arrange
            Integer id = 1;
            VoucherStatus newStatus = anyNonActiveStatus();
            Voucher genericVoucher =
                    createMockVoucher(id, VoucherType.GENERIC, VoucherStatus.ACTIVE, 100, 100);
            UpdatedVoucherRequest request = createUpdateRequest(newStatus, null);
            when(voucherRepository.findById(id)).thenReturn(Optional.of(genericVoucher));

            // Act
            VoucherResponse response = voucherService.update(id, request);

            // Assert
            assertEquals(newStatus.name(), response.getStatus());
            assertEquals(100, response.getQuantityRemain()); // Giữ nguyên
            verify(voucherRepository, times(1)).save(genericVoucher);
        }

        @Test
        @DisplayName("Cập nhật thành công cả status và quantityRemain cho voucher GENERIC")
        void update_Success_Generic_BothFields() {
            // Arrange
            Integer id = 1;
            VoucherStatus newStatus = anyNonActiveStatus();
            Voucher genericVoucher =
                    createMockVoucher(id, VoucherType.GENERIC, VoucherStatus.ACTIVE, 100, 100);
            UpdatedVoucherRequest request = createUpdateRequest(newStatus, 0);
            when(voucherRepository.findById(id)).thenReturn(Optional.of(genericVoucher));

            // Act
            VoucherResponse response = voucherService.update(id, request);

            // Assert
            assertEquals(newStatus.name(), response.getStatus());
            assertEquals(0, response.getQuantityRemain());
            verify(voucherRepository, times(1)).save(genericVoucher);
        }
    }

    // 3. NHÓM TEST CHO HÀM CREATE VOUCHER
    @Nested
    @DisplayName("Tests for create()")
    class CreateVoucherTests {

        @Test
        @DisplayName("Thất bại khi Rule không tồn tại")
        void create_Fail_RuleNotExist() {
            // Arrange
            VoucherRequest request = createValidRequest(VoucherType.GENERIC, 100, "TET2026");
            when(ruleCampaignRepository.findById(10)).thenReturn(Optional.empty());

            // Act & Assert
            AppException ex =
                    assertThrows(AppException.class, () -> voucherService.create(request));
            assertEquals(RuleCampaignErrorCode.RULE_NOT_EXIST, ex.getErrorCode());
            verifyNoInteractions(voucherRepository, voucherInsertHelper);
        }

        @Test
        @DisplayName("Tạo voucher GENERIC thành công (1 bản ghi, code = prefix)")
        void create_Success_Generic() {
            // Arrange
            VoucherRequest request = createValidRequest(VoucherType.GENERIC, 100, "TET2026");
            RuleCampaign rule = createMockRule(10); // Tạo trước, KHÔNG gọi trong when(...)
            when(ruleCampaignRepository.findById(10)).thenReturn(Optional.of(rule));
            when(voucherRepository.save(any(Voucher.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            CreateVoucherResponse response = voucherService.create(request);

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getGenerated());
            assertEquals("completed", response.getStatus());
            assertNull(response.getJobId());

            ArgumentCaptor<Voucher> captor = ArgumentCaptor.forClass(Voucher.class);
            verify(voucherRepository, times(1)).save(captor.capture());
            Voucher saved = captor.getValue();
            assertEquals("TET2026", saved.getCode());
            assertEquals(VoucherType.GENERIC, saved.getType());
            assertEquals(100, saved.getQuantity());
            assertEquals(100, saved.getQuantityRemain());
            assertEquals(VoucherStatus.ACTIVE, saved.getStatus());
            verifyNoInteractions(voucherInsertHelper); // GENERIC không đi qua helper
        }

        @Test
        @DisplayName("Thất bại khi tạo GENERIC bị trùng code")
        void create_Fail_Generic_DuplicateCode() {
            // Arrange
            VoucherRequest request = createValidRequest(VoucherType.GENERIC, 100, "TET2026");
            RuleCampaign rule = createMockRule(10);
            when(ruleCampaignRepository.findById(10)).thenReturn(Optional.of(rule));
            when(voucherRepository.save(any(Voucher.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate"));

            // Act & Assert
            AppException ex =
                    assertThrows(AppException.class, () -> voucherService.create(request));
            assertEquals(VoucherErrorCode.DUPLICATE_CODE, ex.getErrorCode());
        }

        @Test
        @DisplayName("Tạo voucher UNIQUE thành công đủ N voucher")
        void create_Success_Unique() throws Exception {
            // Arrange
            VoucherRequest request = createValidRequest(VoucherType.UNIQUE, 3, "TET");
            RuleCampaign rule = createMockRule(10);
            when(ruleCampaignRepository.findById(10)).thenReturn(Optional.of(rule));
            when(voucherInsertHelper.insertSingle(any(Voucher.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            CreateVoucherResponse response = voucherService.create(request);

            // Assert
            assertEquals(3, response.getGenerated());
            assertEquals(3, response.getVouchers().size());
            assertEquals("completed", response.getStatus());
            assertNull(response.getJobId());

            for (Voucher v : response.getVouchers()) {
                assertTrue(v.getCode().startsWith("TET-"));
                assertEquals("TET-".length() + 8, v.getCode().length());
                assertEquals(VoucherType.UNIQUE, v.getType());
                assertEquals(1, v.getQuantity());
                assertEquals(1, v.getQuantityRemain());
                assertEquals(1, v.getLimitClient());
            }
            // Các code phải khác nhau
            assertEquals(
                    3, response.getVouchers().stream().map(Voucher::getCode).distinct().count());
            verify(voucherInsertHelper, times(3)).insertSingle(any(Voucher.class));
        }

        @Test
        @DisplayName("Tạo voucher UNIQUE không có prefix thì code chỉ gồm 8 ký tự ngẫu nhiên")
        void create_Success_Unique_NoPrefix() throws Exception {
            // Arrange
            VoucherRequest request = createValidRequest(VoucherType.UNIQUE, 1, null);
            RuleCampaign rule = createMockRule(10);
            when(ruleCampaignRepository.findById(10)).thenReturn(Optional.of(rule));
            when(voucherInsertHelper.insertSingle(any(Voucher.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            CreateVoucherResponse response = voucherService.create(request);

            // Assert
            String code = response.getVouchers().get(0).getCode();
            assertEquals(8, code.length());
            assertFalse(code.contains("-"));
            assertEquals(code.toUpperCase(), code);
        }

        @Test
        @DisplayName("Tạo UNIQUE: gặp trùng code thì sinh lại và vẫn đủ số lượng")
        void create_Success_Unique_RetryOnDuplicate() throws Exception {
            // Arrange
            VoucherRequest request = createValidRequest(VoucherType.UNIQUE, 1, "TET");
            RuleCampaign rule = createMockRule(10);
            when(ruleCampaignRepository.findById(10)).thenReturn(Optional.of(rule));
            when(voucherInsertHelper.insertSingle(any(Voucher.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate")) // Lần 1: trùng
                    .thenAnswer(inv -> inv.getArgument(0)); // Lần 2: OK

            // Act
            CreateVoucherResponse response = voucherService.create(request);

            // Assert
            assertEquals(1, response.getGenerated());
            verify(voucherInsertHelper, times(2)).insertSingle(any(Voucher.class));
        }

        @Test
        @DisplayName("Thất bại khi UNIQUE trùng liên tục vượt quá n*3 lần thử")
        void create_Fail_Unique_ExceedMaxAttempts() throws Exception {
            // Arrange
            VoucherRequest request = createValidRequest(VoucherType.UNIQUE, 2, "TET");
            RuleCampaign rule = createMockRule(10);
            when(ruleCampaignRepository.findById(10)).thenReturn(Optional.of(rule));
            when(voucherInsertHelper.insertSingle(any(Voucher.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate"));

            // Act & Assert
            AppException ex =
                    assertThrows(AppException.class, () -> voucherService.create(request));
            assertEquals(VoucherErrorCode.INSUFFICIENT_QUANTITY, ex.getErrorCode());
            verify(voucherInsertHelper, times(6)).insertSingle(any(Voucher.class)); // 2 * 3
        }
    }

    // CÁC HÀM TIỆN ÍCH DÙNG CHUNG
    // Tạo 1 RuleCampaign giả (chỉ cần getId)
    // LƯU Ý: không được gọi hàm này bên trong when(...) / thenReturn(...)
    private RuleCampaign createMockRule(Integer id) {
        RuleCampaign rule = mock(RuleCampaign.class);
        lenient().when(rule.getId()).thenReturn(id);
        return rule;
    }

    // Tạo 1 request tạo voucher hợp lệ
    private VoucherRequest createValidRequest(VoucherType type, int quantity, String prefix) {
        VoucherRequest request = new VoucherRequest();
        request.setRuleId(10);
        request.setType(type);
        request.setQuantity(quantity);
        request.setPrefix(prefix);
        request.setLimitClient(1);
        request.setDistributionChannel(DistributionChannel.WEBHOOK);
        return request;
    }

    // Tạo 1 request cập nhật (truyền null nếu không muốn cập nhật field đó)
    private UpdatedVoucherRequest createUpdateRequest(
            VoucherStatus status, Integer quantityRemain) {
        UpdatedVoucherRequest request = new UpdatedVoucherRequest();
        request.setStatus(status);
        request.setQuantityRemain(quantityRemain);
        return request;
    }

    // Tạo 1 Voucher entity mẫu lấy từ DB
    private Voucher createMockVoucher(
            Integer id, VoucherType type, VoucherStatus status, int quantity, int quantityRemain) {
        RuleCampaign rule = createMockRule(10);
        return Voucher.builder()
                .id(id)
                .code("TET2026")
                .type(type)
                .ruleCampaign(rule)
                .quantity(quantity)
                .quantityRemain(quantityRemain)
                .limitClient(1)
                .distributionChannel(DistributionChannel.WEBHOOK)
                .status(status)
                .build();
    }

    // Lấy 1 status khác ACTIVE (không phụ thuộc tên các giá trị trong enum)
    private VoucherStatus anyNonActiveStatus() {
        return Arrays.stream(VoucherStatus.values())
                .filter(s -> s != VoucherStatus.ACTIVE)
                .findFirst()
                .orElseThrow();
    }
}
