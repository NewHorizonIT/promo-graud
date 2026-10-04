package group2d.promo_graud.modules.voucher.component;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class VoucherJobListener implements JobExecutionListener {

    @Override
    public void afterJob(JobExecution je) {
        long requested = je.getJobParameters().getLong("quantity");
        long written =
                je.getStepExecutions().stream().mapToLong(step -> step.getWriteCount()).sum();

        if (je.getStatus() == BatchStatus.COMPLETED) {
            log.info("Voucher job {} done: {}/{}", je.getId(), written, requested);
        } else {
            log.error(
                    "Voucher job {} {}: {}",
                    je.getId(),
                    je.getStatus(),
                    je.getAllFailureExceptions());
        }
    }
}
