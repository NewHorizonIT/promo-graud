package group2d.promo_graud.modules.fraud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableAsync
public class FraudAsyncConfig {

    @Bean(name = "fraudLogExecutor")
    public ThreadPoolTaskExecutor fraudLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10_000);
        executor.setThreadNamePrefix("fraud-log-");
        // Quá tải thì bỏ log, ưu tiên bảo vệ thread xử lý request
        // (CallerRunsPolicy sẽ bắt thread request tự ghi DB, làm chậm đúng lúc đang bị tấn công)
        executor.setRejectedExecutionHandler(
                (r, pool) ->
                        log.warn(
                                "fraud-log queue full — dropping log task (active={}, queue={})",
                                pool.getActiveCount(),
                                pool.getQueue().size()));
        return executor;
    }
}
