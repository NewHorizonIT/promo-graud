package group2d.promo_graud.modules.voucher.component.worker;

import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.voucher.component.distribute.DistributionPayload;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoucherDistributionEventListener {
    private final StringRedisTemplate redisTemplate;

    // Thuộc tính AFTER_COMMIT ép hàm này phải CHỜ DB lưu xong 100% mới được phép chạy
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVoucherDistributed(VoucherDistributionEvent event) {
        String streamKey = "voucher_distribution_stream";
        for (DistributionPayload payload : event.payloads()) {
            ObjectRecord<String, DistributionPayload> record =
                    StreamRecords.newRecord().in(streamKey).ofObject(payload);
            // gửi vào stream
            redisTemplate.opsForStream().add(record);
        }
        // 2. XỬ LÝ RÁC (MAXLEN): Ép Stream chỉ giữ lại tối đa 10,000 records mới nhất
        redisTemplate.opsForStream().trim(streamKey, 10000);
    }
}
