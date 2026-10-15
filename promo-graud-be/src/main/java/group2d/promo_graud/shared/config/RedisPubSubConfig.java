package group2d.promo_graud.shared.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import group2d.promo_graud.modules.voucher.component.worker.VoucherDistributeWorker;

@Configuration
public class RedisPubSubConfig {
    /**
     * ChannelTopic định nghĩa tên kênh. Phải khớp 100% với tên bạn đã dùng ở lệnh:
     * redisTemplate.convertAndSend("voucher_distribution_topic", jsonPayload);
     */
    @Bean
    public ChannelTopic voucherTopic() {
        return new ChannelTopic("voucher_distribution_topic");
    }

    // 1. Tạo Thread Pool chuyên xử lý nhiệm vụ gửi mail
    @Bean(name = "workerTaskExecutor")
    public Executor workerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // Chạy song song 4 luồng cùng lúc
        executor.setMaxPoolSize(8); // Tối đa 8 luồng khi số lượng message quá lớn
        executor.setQueueCapacity(5000); // Hàng chờ chứa message
        // Chính sách an toàn: Nếu xui xẻo hàng chờ 5000 bị đầy, luồng chính sẽ tự tay xử lý nốt
        // thay vì quăng Exception làm mất mail
        executor.setRejectedExecutionHandler(
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("email-worker-");
        executor.initialize();
        return executor;
    }

    /**
     * Container này giống như một "Tổng đài", nó quản lý các đường truyền mạng. Khi có message từ
     * 'voucherTopic', nó sẽ tự động đẩy sang cho 'worker' xử lý.
     */
    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            VoucherDistributeWorker worker,
            ChannelTopic voucherTopic,
            Executor workerTaskExecutor) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // KÍCH HOẠT ĐA LUỒNG: Đẩy việc xử lý onMessage() cho Thread Pool
        container.setTaskExecutor(workerTaskExecutor);
        // Đăng ký: Cho phép worker lắng nghe trên kênh voucherTopic
        container.addMessageListener(worker, voucherTopic);

        return container;
    }
}
