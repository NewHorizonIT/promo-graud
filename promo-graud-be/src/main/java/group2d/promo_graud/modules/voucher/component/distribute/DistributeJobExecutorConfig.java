package group2d.promo_graud.modules.voucher.component.distribute;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DistributeJobExecutorConfig {
    // 1. Tạo một Hồ chứa luồng (ThreadPool) DÀNH RIÊNG cho việc Phân phối
    @Bean("distributeTaskExecutor")
    public ThreadPoolTaskExecutor distributeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50); // Hàng đợi 50 slot
        executor.setThreadNamePrefix(
                "distribute-batch-"); // Đổi tên luồng để dễ debug trong Console
        executor.initialize();
        return executor;
    }

    // 2. Tạo một Người vận hành Job (JobOperator) cầm cái ThreadPool riêng ở trên
    @Bean("asyncDistributeJobOperator")
    public JobOperator asyncDistributeJobOperator(
            JobRepository jobRepository,
            JobRegistry jobRegistry,
            @Qualifier("distributeTaskExecutor") ThreadPoolTaskExecutor distributeExecutor)
            throws Exception {

        TaskExecutorJobOperator operator = new TaskExecutorJobOperator();
        operator.setJobRepository(jobRepository);
        operator.setJobRegistry(jobRegistry);
        operator.setTaskExecutor(distributeExecutor); // Nhét cái luồng to bự vào đây
        operator.afterPropertiesSet();

        return operator;
    }
}
