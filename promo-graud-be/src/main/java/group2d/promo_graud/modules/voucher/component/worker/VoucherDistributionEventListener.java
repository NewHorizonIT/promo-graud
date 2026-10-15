package group2d.promo_graud.modules.voucher.component.worker;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.voucher.component.distribute.DistributionPayload;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoucherDistributionEventListener {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // Thuộc tính AFTER_COMMIT ép hàm này phải CHỜ DB lưu xong 100% mới được phép chạy
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVoucherDistributed(VoucherDistributionEvent event) {
        for (DistributionPayload payload : event.payloads()) {
            try {
                String jsonPayload = objectMapper.writeValueAsString(payload);
                redisTemplate.convertAndSend("voucher_distribution_topic", jsonPayload);
                log.info(
                        "Đã bắn event phân phối cho Email: {} với mã {}",
                        payload.getUserEmail(),
                        payload.getVoucherCode());
            } catch (Exception e) {
                log.error("Lỗi khi bắn event phân phối voucher lên Redis", e);
            }
        }
    }
}
