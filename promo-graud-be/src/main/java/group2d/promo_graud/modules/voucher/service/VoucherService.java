package group2d.promo_graud.modules.voucher.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.RuleCampaignRepository;
import group2d.promo_graud.modules.rules.enums.RuleCampaignErrorCode;
import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.VoucherRepository;
import group2d.promo_graud.modules.voucher.dto.requests.UpdatedVoucherRequest;
import group2d.promo_graud.modules.voucher.dto.requests.VoucherRequest;
import group2d.promo_graud.modules.voucher.dto.responses.CreateVoucherResponse;
import group2d.promo_graud.modules.voucher.dto.responses.JobStatusResponse;
import group2d.promo_graud.modules.voucher.dto.responses.VoucherResponse;
import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.VoucherErrorCode;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;
import group2d.promo_graud.shared.dto.PaginatedResponse;
import group2d.promo_graud.shared.exception.AppException;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherInsertHelper voucherInsertHelper;

    private final VoucherBatchService voucherBatchService;

    private final VoucherRepository voucherRepository;
    private final RuleCampaignRepository ruleCampaignRepository;
    private final JobRepository jobRepository;

    @Value("${voucher.batch.unique-threshold:50}")
    private int uniqueThreshold;

    // -----------------------Lấy danh sách voucher------------------------
    public PaginatedResponse<VoucherResponse> getAll(
            String type,
            String status,
            Integer ruleId,
            String distributionChannel,
            int page,
            int pageSize) {

        VoucherType typeEnum = (type != null) ? VoucherType.valueOf(type.toUpperCase()) : null;

        VoucherStatus statusEnum =
                (status != null) ? VoucherStatus.valueOf(status.toUpperCase()) : null;

        DistributionChannel channelEnum =
                (distributionChannel != null)
                        ? DistributionChannel.valueOf(distributionChannel.toUpperCase())
                        : null;
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<Voucher> result =
                voucherRepository.getAll(typeEnum, statusEnum, ruleId, channelEnum, pageable);

        return PaginatedResponse.<VoucherResponse>builder()
                .data(result.getContent().stream().map(VoucherResponse::fromEntity).toList())
                .page(page)
                .size(pageSize)
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    // -----------------------Cập nhật voucher-----------------------------
    // Voucher unique thì mới cho cập nhật số lương
    public VoucherResponse update(Integer id, UpdatedVoucherRequest request) {
        if (request.getStatus() == null && request.getQuantityRemain() == null) {
            throw new AppException(VoucherErrorCode.REQUIRED_FIELD);
        }
        Voucher voucher =
                voucherRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(VoucherErrorCode.NOT_EXIST));
        if (voucher.getType() == VoucherType.UNIQUE && request.getQuantityRemain() != null) {
            throw new AppException(VoucherErrorCode.GENERIC_QUANTITY_NOT_EDITABLE);
        }
        if (voucher.getType() == VoucherType.GENERIC
                && request.getQuantityRemain() != null
                && request.getQuantityRemain() > voucher.getQuantity()) {
            throw new AppException(VoucherErrorCode.INSUFFICIENT_QUANTITY);
        }
        if (request.getQuantityRemain() != null) {
            voucher.setQuantityRemain(request.getQuantityRemain());
        }
        if (request.getStatus() != null) {
            voucher.setStatus(request.getStatus());
        }
        voucherRepository.save(voucher);
        return VoucherResponse.builder()
                .code(voucher.getCode())
                .ruleId(voucher.getRuleCampaign().getId())
                .distributionChannel(voucher.getDistributionChannel().name())
                .status(voucher.getStatus().name())
                .quantity(voucher.getQuantity())
                .quantityRemain(voucher.getQuantityRemain())
                .limitClient(voucher.getLimitClient())
                .build();
    }

    // ------------------------Kiểm tra trạng thái job(AI)----------------------
    public JobStatusResponse getJobStatus(Long jobId) {
        JobExecution je = jobRepository.getJobExecution(jobId);
        if (je == null) {
            throw new AppException(VoucherErrorCode.BATCH_JOB_NOT_FOUND);
        }

        long written =
                je.getStepExecutions().stream().mapToLong(step -> step.getWriteCount()).sum();

        Long requestedParam = je.getJobParameters().getLong("quantity");
        long requested = requestedParam != null ? requestedParam : 0L;

        return JobStatusResponse.builder()
                .errorMessage(resolveErrorMessage(je))
                .jobId(jobId)
                .status(je.getStatus().name())
                .processed(written)
                .requested(requested)
                .build();
    }

    private String resolveErrorMessage(JobExecution je) {
        if (je.getStatus() != BatchStatus.FAILED) {
            return null;
        }
        // log chi tiết ở server, chỉ trả message chung cho client
        je.getAllFailureExceptions()
                .forEach(e -> log.error("Voucher job {} failed", je.getId(), e));
        return "Voucher generation failed";
    }

    // -----------------------Tạo voucher----------------------------------
    public CreateVoucherResponse create(VoucherRequest request) {

        RuleCampaign rule =
                ruleCampaignRepository
                        .findById(request.getRuleId())
                        .orElseThrow(() -> new AppException(RuleCampaignErrorCode.RULE_NOT_EXIST));

        // GENERIC
        if (request.getType() == VoucherType.GENERIC) {

            List<Voucher> voucherList = createGeneric(request, rule);
            return CreateVoucherResponse.builder()
                    .generated(voucherList.size())
                    .vouchers(voucherList)
                    .status("completed")
                    .jobId(null)
                    .build();
        }
        if (request.getQuantity() >= uniqueThreshold) {
            Long jobId = voucherBatchService.triggerGenerateVoucherJob(request, rule);
            return CreateVoucherResponse.builder()
                    .generated(request.getQuantity())
                    .vouchers(List.of())
                    .status("processing")
                    .jobId(jobId)
                    .build();
        }

        List<Voucher> voucherList = createUniqueSync(request, rule);
        return CreateVoucherResponse.builder()
                .generated(voucherList.size())
                .vouchers(voucherList)
                .status("completed")
                .jobId(null)
                .build();
    }

    // Insert thẳng 1 bản ghi vào db (GENERIC)
    private List<Voucher> createGeneric(VoucherRequest request, RuleCampaign rule) {
        Voucher voucher =
                Voucher.builder()
                        .code(request.getPrefix())
                        .type(VoucherType.GENERIC)
                        .ruleCampaign(rule)
                        .quantity(request.getQuantity())
                        .quantityRemain(request.getQuantity())
                        .limitClient(request.getLimitClient())
                        .distributionChannel(request.getDistributionChannel())
                        .status(VoucherStatus.ACTIVE)
                        .build();

        try {
            Voucher saved = voucherRepository.save(voucher);
            return List.of(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(VoucherErrorCode.DUPLICATE_CODE);
        }
    }

    // Insert thẳng N voucher vào db (UNIQUE)
    private List<Voucher> createUniqueSync(VoucherRequest request, RuleCampaign rule) {

        int quantity = request.getQuantity();
        List<Voucher> listVoucher = new ArrayList<>();

        int totalAttempts = 0; // index
        int maxTotalAttempts = quantity * 3; // Giới hạn n*3 lần chạy.

        while (listVoucher.size() < quantity && totalAttempts < maxTotalAttempts) {
            totalAttempts++;

            String code = generateCode(request.getPrefix());

            Voucher voucher =
                    Voucher.builder()
                            .code(code)
                            .type(VoucherType.UNIQUE)
                            .ruleCampaign(rule)
                            .quantity(1)
                            .quantityRemain(1)
                            .limitClient(1)
                            .distributionChannel(request.getDistributionChannel())
                            .status(VoucherStatus.ACTIVE)
                            .build();

            try {
                listVoucher.add(voucherInsertHelper.insertSingle(voucher));
            } catch (DataIntegrityViolationException ex) {
                log.warn("Voucher is duplicate , system is creating again");
            }
        }

        if (listVoucher.size() < quantity) {
            log.error("Only create {}/{} voucher", listVoucher.size(), quantity);
            throw new AppException(VoucherErrorCode.INSUFFICIENT_QUANTITY);
        }

        return listVoucher;
    }

    // Sinh code
    // Code = prefix + 8 ký tự của UUID ngẫu nhiên vd: TET2026_ABCD21SC
    private String generateCode(String prefix) {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return StringUtils.hasText(prefix) ? prefix + "-" + random : random;
    }
}
