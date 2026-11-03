package group2d.promo_graud.shared.config;

import java.time.Duration;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.voucher.component.distribute.DistributionPayload;
import group2d.promo_graud.modules.voucher.component.worker.VoucherDistributeWorker;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RedisStreamConfig {
    private final StringRedisTemplate redisTemplate;

    private static final String STREAM_KEY = "voucher_distribution_stream";
    private static final String CONSUMER_GROUP = "email-group";

    @PostConstruct
    public void initConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, CONSUMER_GROUP);
            log.info("Đã tạo thành công Consumer Group: {}", CONSUMER_GROUP);
        } catch (Exception e) {
            log.info("Consumer Group '{}' đã sẵn sàng hoạt động.", CONSUMER_GROUP);
        }
    }

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, DistributionPayload>>
            streamContainer(RedisConnectionFactory factory, VoucherDistributeWorker worker) {

        // 1. Tạo ThreadPool cho các luồng xử lý email
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("stream-worker-");
        executor.initialize();

        // 2. Cấu hình Container
        StreamMessageListenerContainerOptions<String, ObjectRecord<String, DistributionPayload>>
                options =
                        StreamMessageListenerContainerOptions.builder()
                                .pollTimeout(Duration.ofMillis(100)) // Tần suất quét tin nhắn mới
                                .executor(executor)
                                .targetType(
                                        DistributionPayload
                                                .class) // Chỉ định kiểu Object để tự động parse
                                .build();

        // 3. Tạo Container
        StreamMessageListenerContainer<String, ObjectRecord<String, DistributionPayload>>
                container = StreamMessageListenerContainer.create(factory, options);

        // 4. Đăng ký nhận tin nhắn (Manual ACK thông qua lệnh receive)
        // Vì group đã chắc chắn được tạo ở @PostConstruct nên chỗ này cực kỳ an toàn
        container.receive(
                Consumer.from(CONSUMER_GROUP, "worker-1"),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
                worker);

        container.start();
        return container;
    }
}
