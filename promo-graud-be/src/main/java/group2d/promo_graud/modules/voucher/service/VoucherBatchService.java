package group2d.promo_graud.modules.voucher.service;

import java.util.UUID;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.voucher.dto.requests.VoucherRequest;
import group2d.promo_graud.modules.voucher.enums.VoucherErrorCode;
import group2d.promo_graud.shared.exception.AppException;

// Taọ tiến trình chạy ngầm
@Service
@Slf4j
public class VoucherBatchService {
    // Interface cung cấp giao diện khởi chạy và quản lý job
    @Qualifier("asyncJobOperator")
    private final JobOperator jobOperator;

    // Kịch bản job
    private final Job generateVoucherJob;

    public VoucherBatchService(
            @Qualifier("asyncJobOperator") JobOperator jobOperator, Job generateVoucherJob) {
        this.jobOperator = jobOperator;
        this.generateVoucherJob = generateVoucherJob;
    }

    public Long triggerGenerateVoucherJob(VoucherRequest req, RuleCampaign rule) {
        log.info(
                "operator={}, thread={}", jobOperator.getClass(), Thread.currentThread().getName());
        // Tập hợp các tham số cấu hình cho job
        JobParameters params =
                new JobParametersBuilder()
                        .addString("requestId", UUID.randomUUID().toString())
                        .addLong("ruleId", rule.getId().longValue())
                        .addLong("quantity", (long) req.getQuantity())
                        .addLong("limitClient", (long) req.getLimitClient())
                        .addString("channel", req.getDistributionChannel().name())
                        .addString("prefix", req.getPrefix())
                        .toJobParameters();
        try {
            return jobOperator.start(generateVoucherJob, params).getId();
        } catch (Exception e) {
            log.error("Cannot start voucher generation job", e);
            throw new AppException(VoucherErrorCode.BATCH_TRIGGER_FAILED);
        }
    }
}
