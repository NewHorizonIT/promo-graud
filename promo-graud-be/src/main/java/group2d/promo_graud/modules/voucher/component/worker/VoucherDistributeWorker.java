package group2d.promo_graud.modules.voucher.component.worker;

import java.time.LocalDateTime;

import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.voucher.component.distribute.DistributionPayload;
import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.DistributionLogStatus;
import group2d.promo_graud.modules.voucher.repository.DistributionLogRepository;
import group2d.promo_graud.modules.voucher.service.DevEmailService;
import group2d.promo_graud.modules.voucher.service.WebHookService;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoucherDistributeWorker
        implements StreamListener<String, ObjectRecord<String, DistributionPayload>> {
    private final DevEmailService emailService;
    private final DistributionLogRepository logRepository;
    private final WebHookService webHookService;
    private final StringRedisTemplate redisTemplate;

    /** Hàm này tự động được trigger (kích hoạt) mỗi khi có 1 message gửi vào Redis Topic */
    @Override
    @Transactional
    public void onMessage(ObjectRecord<String, DistributionPayload> message) {
        // 1 & 2. LẤY TRỰC TIẾP OBJECT TỪ MESSAGE (Không cần ép kiểu hay parse JSON bằng tay)
        DistributionPayload payload =
                message.getValue(); // Lấy thẳng ra Object, không cần tự parse JSON nữa!
        try {

            log.info(
                    "Worker nhận nhiệm vụ gửi Voucher {} cho Email: {}",
                    payload.getVoucherCode(),
                    payload.getUserEmail());

            // 3. Phân luồng xử lý theo Channel
            if (payload.getChannel() == DistributionChannel.EMAIL) {
                emailService.sendVoucher(payload.getUserEmail(), payload.getVoucherCode());
            } else if (payload.getChannel() == DistributionChannel.WEBHOOK) {
                log.info(
                        "Worker bắn WEBHOOK mã {} cho {}",
                        payload.getVoucherCode(),
                        payload.getUserEmail());

                String partnerWebhookUrl = payload.getTargetUrl();
                webHookService.sendVoucherWebHook(
                        partnerWebhookUrl, payload.getUserEmail(), payload.getVoucherCode());
            }
            // 4. Thành công -> Update trạng thái Log xuống Database (Chỉ tốn 1 lệnh UPDATE)
            logRepository.updateStatus(payload.getLogId(), DistributionLogStatus.SENT);

            // GỌI LỆNH XACK: Báo cáo Redis đã xong việc để xóa tin nhắn khỏi PEL
            redisTemplate.opsForStream().acknowledge("email-group", message);
        } catch (Exception e) {
            // 5. Nếu gửi lỗi (sai email, rớt mạng...) -> Update trạng thái FAILED
            if (payload.getLogId() != null) {
                // Rút gọn thông báo lỗi để tránh tràn cột TEXT trong DB (nếu stacktrace quá dài)
                String errorMsg =
                        e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                if (errorMsg.length() > 500) {
                    errorMsg = errorMsg.substring(0, 500);
                }
                // Cập nhật DB: Chuyển sang FAILED, Tăng retry_count + 1, Lưu log lỗi
                logRepository.markAsFailedAndIncrementRetry(
                        payload.getLogId(),
                        DistributionLogStatus.FAILED,
                        LocalDateTime.now(),
                        errorMsg);
            }
        }
    }
}
