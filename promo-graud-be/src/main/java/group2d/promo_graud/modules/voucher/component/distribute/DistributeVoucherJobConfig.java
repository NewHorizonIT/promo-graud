package group2d.promo_graud.modules.voucher.component.distribute;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DistributeVoucherJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // Lấy chunk-size từ file application.yml (mặc định 50 nếu không cấu hình)
    @Value("${voucher.batch.chunk-size:50}")
    private int chunkSize;

    @Bean
    public Step distributeVoucherStep(
            DistributeVoucherReader reader, DistributeVoucherWriter writer) {

        ChunkOrientedStepBuilder<Integer, Integer> stepBuilder =
                new StepBuilder("distributeVoucherStep", jobRepository)
                        .<Integer, Integer>chunk(chunkSize)
                        .transactionManager(transactionManager)
                        .reader(reader)
                        .writer(writer)
                        .faultTolerant()
                        .retryLimit(3)
                        .retry(DataAccessException.class);

        return stepBuilder.build();
    }

    @Bean
    public Job distributeVoucherJob(Step distributeVoucherStep) {
        return new JobBuilder("distributeVoucherJob", jobRepository)
                .start(distributeVoucherStep)
                .build();
    }
}
