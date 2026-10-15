package group2d.promo_graud.modules.voucher.component.worker;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
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
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoucherDistributeWorker implements MessageListener {
    private final ObjectMapper objectMapper;
    private final DevEmailService emailService;
    private final DistributionLogRepository logRepository;
    //  private final ProdEmailService prodEmailService;
    private final WebHookService webHookService;

    /** Hàm này tự động được trigger (kích hoạt) mỗi khi có 1 message gửi vào Redis Topic */
    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        DistributionPayload payload = null;
        try {
            // 1. Chuyển đổi dữ liệu nhị phân (byte[]) từ Redis về chuỗi JSON
            String jsonPayload = new String(message.getBody());

            // 2. Ép kiểu JSON về Object Java
            payload = objectMapper.readValue(jsonPayload, DistributionPayload.class);
            log.info(
                    "Worker nhận nhiệm vụ gửi Voucher {} cho Email: {}",
                    payload.getVoucherCode(),
                    payload.getUserEmail());

            // 3. Phân luồng xử lý theo Channel
            if (payload.getChannel() == DistributionChannel.EMAIL) {
                // Gọi sang Service gửi mail cho dev
                emailService.sendVoucher(payload.getUserEmail(), payload.getVoucherCode());
                // Gọi sang Service gửi mail cho prod
                // prodEmailService.sendVoucher(payload.getUserEmail(), payload.getVoucherCode());
            } else if (payload.getChannel() == DistributionChannel.WEBHOOK) {
                log.info(
                        "Worker bắn WEBHOOK mã {} cho {}",
                        payload.getVoucherCode(),
                        payload.getUserEmail());

                String partnerWebhookUrl = payload.getTargetUrl();
                webHookService.sendVoucherWebhook(
                        partnerWebhookUrl, payload.getUserEmail(), payload.getVoucherCode());
            }
            // 4. Thành công -> Update trạng thái Log xuống Database (Chỉ tốn 1 lệnh UPDATE)
            int updatedRows =
                    logRepository.updateStatus(payload.getLogId(), DistributionLogStatus.SENT);
            log.info(
                    "Cập nhật trạng thái SENT: logId={}, updatedRows={}",
                    payload.getLogId(),
                    updatedRows);

        } catch (Exception e) {
            // 5. Nếu gửi lỗi (sai email, rớt mạng...) -> Update trạng thái FAILED
            log.error(
                    "Lỗi xử lý message. logId={}, email={}",
                    payload != null ? payload.getLogId() : null,
                    payload != null ? payload.getUserEmail() : null,
                    e);
            // Kiểm tra payload khác null để chắc chắn nó đã được parse thành công trước khi lỗi gửi
            // mail
            if (payload != null && payload.getLogId() != null) {
                logRepository.updateStatus(payload.getLogId(), DistributionLogStatus.FAILED);
            }
        }
    }
}
