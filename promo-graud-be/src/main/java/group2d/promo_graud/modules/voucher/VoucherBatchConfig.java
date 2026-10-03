package group2d.promo_graud.modules.voucher;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class VoucherBatchConfig {

  @Bean("asyncJobOperator")
  public JobOperator asyncJobOperator(JobRepository jobRepository,
                                      JobRegistry jobRegistry) throws Exception {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(20);
    executor.setThreadNamePrefix("voucher-batch-");
    executor.initialize();

    TaskExecutorJobOperator operator = new TaskExecutorJobOperator();
    operator.setJobRepository(jobRepository);
    operator.setJobRegistry(jobRegistry);
    operator.setTaskExecutor(executor);
    operator.afterPropertiesSet();
    return operator;
  }
  @Bean
  public JobRegistry jobRegistry() {
    return new MapJobRegistry();
  }
}
