package group2d.promo_graud.modules.voucher.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class WebHookService {
    private final RestClient restClient;

    public void sendVoucherWebhook(String targetUrl, String userEmail, String voucherCode) {
        // Đóng gói dữ liệu (Payload) theo format mà đối tác yêu cầu
        Map<String, Object> webhookPayload =
                Map.of(
                        "event", "VOUCHER_DISTRIBUTED",
                        "timestamp", System.currentTimeMillis(),
                        "data",
                                Map.of(
                                        "email", userEmail,
                                        "voucher_code", voucherCode));

        try {
            restClient.post().uri(targetUrl).body(webhookPayload).retrieve().toBodilessEntity();

            log.info("Webhook gửi thành công mã {} tới {}", voucherCode, targetUrl);
        } catch (Exception e) {
            log.error("Lỗi khi gửi Webhook tới {}: {}", targetUrl, e.getMessage());
            // Ném lỗi để Worker bắt được và update DB thành FAILED
            throw new RuntimeException("Webhook Delivery Failed", e);
        }
    }
}
