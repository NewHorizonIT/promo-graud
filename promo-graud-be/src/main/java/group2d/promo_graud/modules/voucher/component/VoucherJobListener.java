package group2d.promo_graud.modules.voucher.component;

import java.util.Set;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherJobListener implements JobExecutionListener {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void afterJob(JobExecution je) {
        long requested = je.getJobParameters().getLong("quantity");
        Long ruleId = je.getJobParameters().getLong("ruleId");
        long written =
                je.getStepExecutions().stream().mapToLong(step -> step.getWriteCount()).sum();

        if (je.getStatus() == BatchStatus.COMPLETED) {
            log.info("Voucher job {} done: {}/{}", je.getId(), written, requested);
            if (ruleId != null) {
                // 1. Định nghĩa chuỗi Pattern quét toàn bộ (Có dấu * ở cuối)
                // Ví dụ: "voucher_count:rule_1*" sẽ quét trúng cả channel_EMAIL và channel_WEBHOOK
                String pattern = "voucher_count:rule_" + ruleId + "*";

                // 2. Tìm tất cả các key khớp với pattern
                Set<String> keysToDelete = redisTemplate.keys(pattern);

                // 3. Xóa một lượt nếu tìm thấy
                if (keysToDelete != null && !keysToDelete.isEmpty()) {
                    redisTemplate.delete(keysToDelete);
                    log.info(
                            "Đã dọn sạch {} cache Redis để đếm lại DB cho ruleId={}",
                            keysToDelete.size(),
                            ruleId);
                } else {
                    log.info("Không có cache Redis cũ nào cần dọn dẹp cho ruleId={}", ruleId);
                }
            }
        } else {
            log.error(
                    "Voucher job {} {}: {}",
                    je.getId(),
                    je.getStatus(),
                    je.getAllFailureExceptions());
        }
    }
}
