package group2d.promo_graud.modules.voucher.component;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.RequiredArgsConstructor;

/** Ghi đè cấu hình của spring boot , chuyển sang chạy bất đồng bộ */
@Configuration
@RequiredArgsConstructor
public class VoucherBatchConfig {

    @Bean("asyncJobOperator")
    public JobOperator asyncJobOperator(JobRepository jobRepository, JobRegistry jobRegistry)
            throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // số luồng cơ bản
        executor.setMaxPoolSize(4); // số luồng tối đa
        executor.setQueueCapacity(20); // số job trong hàng đợi
        executor.setThreadNamePrefix("voucher-batch-"); // tên luồng vd voucher-batch-1...
        executor.initialize();

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
