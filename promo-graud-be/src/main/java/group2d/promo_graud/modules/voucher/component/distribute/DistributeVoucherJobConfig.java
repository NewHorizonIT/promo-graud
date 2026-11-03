package group2d.promo_graud.modules.voucher.component.distribute;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DistributeVoucherJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // 1. Tạo một ThreadPool riêng để chạy CÁC CHUNK SONG SONG
    @Bean("stepTaskExecutor")
    public ThreadPoolTaskExecutor stepTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 5 luồng cùng bốc chunk
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("step-worker-");
        executor.initialize();
        return executor;
    }

    // Lấy chunk-size từ file application.yml (mặc định 50 nếu không cấu hình)
    @Value("${voucher.batch.chunk-size:50}")
    private int chunkSize;

    @Bean
    public Step distributeVoucherStep(
            DistributeVoucherReader reader,
            DistributeVoucherWriter writer,
            @Qualifier("stepTaskExecutor") ThreadPoolTaskExecutor stepExecutor) {

        return new StepBuilder("distributeVoucherStep", jobRepository)
                .<Integer, Integer>chunk(chunkSize, transactionManager) // Gộp 2 tham số
                .reader(reader)
                .writer(writer)
                .taskExecutor(stepExecutor) // Giao việc cho 5 công nhân
                .build();
    }

    @Bean
    public Job distributeVoucherJob(Step distributeVoucherStep) {
        return new JobBuilder("distributeVoucherJob", jobRepository)
                .start(distributeVoucherStep)
                .build();
    }
}
