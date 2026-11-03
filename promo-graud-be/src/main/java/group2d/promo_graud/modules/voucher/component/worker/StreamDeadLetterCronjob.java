package group2d.promo_graud.modules.voucher.component.worker;

import java.time.Duration;
import java.util.List;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.voucher.component.distribute.DistributionPayload;
import group2d.promo_graud.modules.voucher.enums.DistributionLogStatus;
import group2d.promo_graud.modules.voucher.repository.DistributionLogRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class StreamDeadLetterCronjob {
    private final StringRedisTemplate redisTemplate;
    private final VoucherDistributeWorker worker;
    private final DistributionLogRepository logRepository;

    // Chạy ngầm mỗi 5 phút một lần
    @Scheduled(fixedDelay = 300000)
    public void processStuckMessages() {
        String streamKey = "voucher_distribution_stream";
        String group = "email-group";
        String workerName = "worker-1";
        // XPENDING: Tìm các tin nhắn kẹt trong PEL quá 10 phút
        PendingMessages pendingMessages =
                redisTemplate
                        .opsForStream()
                        .pending(
                                streamKey,
                                Consumer.from(group, workerName),
                                Range.unbounded(),
                                100 // Lấy tối đa 100 tin nhắn mỗi lần quét
                                );
        for (PendingMessage pending : pendingMessages) {
            Duration idleTime = pending.getElapsedTimeSinceLastDelivery();
            if (idleTime.toMinutes() >= 10) {
                String recordId = pending.getIdAsString();
                long deliveryCount = pending.getTotalDeliveryCount();

                if (deliveryCount >= 3) {
                    // XỬ LÝ DLQ (Dead Letter Queue): Đã thử gửi 3 lần vẫn lỗi
                    log.error("Message {} kẹt quá 3 lần. Đẩy vào DLQ!", recordId);

                    List<ObjectRecord<String, DistributionPayload>> records =
                            redisTemplate
                                    .opsForStream()
                                    .range(
                                            DistributionPayload.class,
                                            streamKey,
                                            Range.closed(recordId, recordId));

                    if (records != null && !records.isEmpty()) {
                        DistributionPayload payload = records.getFirst().getValue();

                        logRepository.updateStatus(payload.getLogId(), DistributionLogStatus.DLQ);
                    }
                    // Gạch nợ luôn để nó không bao giờ hiện lên nữa
                    redisTemplate.opsForStream().acknowledge(streamKey, group, recordId);
                } else {
                    // XCLAIM: Cướp lại quyền xử lý và giao cho Worker này làm lại
                    RedisStreamCommands.XClaimOptions claimOptions =
                            RedisStreamCommands.XClaimOptions.minIdle(Duration.ofMinutes(10))
                                    .ids(recordId);

                    redisTemplate.opsForStream().claim(streamKey, group, workerName, claimOptions);
                    // 2. Lấy nội dung tin nhắn ra dưới dạng ObjectRecord chuẩn Spring
                    List<ObjectRecord<String, DistributionPayload>> records =
                            redisTemplate
                                    .opsForStream()
                                    .range(
                                            DistributionPayload.class,
                                            streamKey,
                                            Range.closed(recordId, recordId));

                    // 3. Tự tay nhét lại vào hàm onMessage() của Worker
                    if (records != null && !records.isEmpty()) {
                        worker.onMessage(records.getFirst());
                    }
                }
            }
        }
    }
}
