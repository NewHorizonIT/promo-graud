package group2d.promo_graud.modules.voucher.component;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.RequiredArgsConstructor;

/** Ghi đè cấu hình của spring boot , chuyển sang chạy bất đồng bộ */
@Configuration
@RequiredArgsConstructor
public class VoucherBatchConfig {

    @Bean("batchTaskExecutor")
    public ThreadPoolTaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("voucher-batch-");
        executor.initialize();
        return executor;
    }

    @Bean("asyncJobOperator")
    public JobOperator asyncJobOperator(
            JobRepository jobRepository,
            JobRegistry jobRegistry,
            @Qualifier("batchTaskExecutor") ThreadPoolTaskExecutor executor)
            throws Exception {

        TaskExecutorJobOperator operator = new TaskExecutorJobOperator();
        operator.setJobRepository(jobRepository);
        operator.setJobRegistry(jobRegistry);
        operator.setTaskExecutor(executor); // chuyển sang chạy bất đồng bộ
        operator.afterPropertiesSet(); // kiểm tra đã đầy đủ tham số cần thiết chưa để khởi tạo (do
        // cấu hinh thủ cong)
        return operator;
    }

    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }
}
