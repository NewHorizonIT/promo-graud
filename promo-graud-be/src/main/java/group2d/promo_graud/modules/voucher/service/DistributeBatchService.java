package group2d.promo_graud.modules.voucher.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.voucher.dto.requests.DistributeVoucherRequest;
import group2d.promo_graud.modules.voucher.enums.VoucherErrorCode;
import group2d.promo_graud.shared.exception.AppException;

@Service
@Slf4j
public class DistributeBatchService {
    // CHỈ cần Tiêm (Inject) Operator riêng của bạn và Job Phân phối
    private final JobOperator distributeJobOperator;
    private final Job distributeVoucherJob;
    private final StringRedisTemplate redisTemplate;

    public DistributeBatchService(
            @Qualifier("asyncDistributeJobOperator") JobOperator distributeJobOperator,
            @Qualifier("distributeVoucherJob") Job distributeVoucherJob,
            StringRedisTemplate redisTemplate) {
        this.distributeJobOperator = distributeJobOperator;
        this.distributeVoucherJob = distributeVoucherJob;
        this.redisTemplate = redisTemplate;
    }

    public Long launchDistributionJob(DistributeVoucherRequest req) {
        log.info(
                "Bắt đầu khởi chạy Job phân phối voucher cho ruleId={}, operator={}, thread={}",
                req.getRuleId(),
                distributeJobOperator.getClass(),
                Thread.currentThread().getName());
        String requestId = UUID.randomUUID().toString();
        // Đóng gói các tham số để truyền sang Spring Batch qua JobParameters
        JobParameters params =
                new JobParametersBuilder()
                        .addString("requestId", requestId)
                        .addLong("ruleId", req.getRuleId().longValue())
                        .addString("channel", req.getDistributionChannel().name())
                        .addLong(
                                "quantity",
                                (long)
                                        req.getUserIds()
                                                .size()) // để quantity do test postman nó lấy giá
                        // trị này chứ mặc định là userIds
                        .toJobParameters();
        try {
            // TỐI ƯU: Đẩy toàn bộ danh sách User ID vào Redis List vì Database sẽ báo lỗi quá giới
            // hạn độ dài chuỗi
            // Chuyển List<Integer> thành List<String> để lưu Redis
            String listKey = "batch_users:" + requestId;
            List<String> usedIdsStr = req.getUserIds().stream().map(String::valueOf).toList();

            // Push hàng loạt vào đuôi hàng đợi
            redisTemplate.opsForList().rightPushAll(listKey, usedIdsStr);
            // Đặt thời gian sống (TTL) cho hàng đợi để dọn rác
            redisTemplate.expire(listKey, 1, TimeUnit.HOURS);
            // Start job
            return distributeJobOperator.start(distributeVoucherJob, params).getId();
        } catch (Exception e) {
            log.error("Không thể khởi chạy Job phân phối voucher", e);
            throw new AppException(VoucherErrorCode.BATCH_TRIGGER_FAILED);
        }
    }
}
