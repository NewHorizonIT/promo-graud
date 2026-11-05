package group2d.promo_graud.modules.voucher.service;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class WebHookService {
    private final RestClient restClient;

    @Value("${promo-graud.webhook.discord-url}")
    private String defaultDiscordUrl;

    @Value("${promo-graud.webhook.secret-key}")
    private String secretKey;

    public void sendVoucherWebHook(String partnerUrl, String partnerUserId, String voucherCode) {
        String targetUrl =
                (partnerUrl == null || partnerUrl.isBlank() || partnerUrl.equals("DISCORD_DEMO"))
                        ? defaultDiscordUrl
                        : partnerUrl;

        try {
            // Chuẩn JSON Discord Embeds
            String jsonPayload =
                    """
                    {
                      "username": "PromoGuard B2B System",
                      "avatar_url": "https://cdn-icons-png.flaticon.com/512/2038/2038768.png",
                      "embeds": [
                        {
                          "title": "🎉 Đã cấp phát Voucher mới!",
                          "color": 5814783,
                          "fields": [
                            {
                              "name": "👤 Mã Khách Hàng",
                              "value": "`%s`",
                              "inline": true
                            },
                            {
                              "name": "🎟️ Mã Voucher",
                              "value": "`%s`",
                              "inline": true
                            }
                          ],
                          "footer": {
                            "text": "Phân phối bởi PromoGuard Engine • %s"
                          }
                        }
                      ]
                    }
                    """
                            .formatted(
                                    partnerUserId, voucherCode, java.time.Instant.now().toString());

            String signature = calculateHmacSha256(jsonPayload, secretKey);

            restClient
                    .post()
                    .uri(targetUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-PromoGuard-Signature", signature)
                    .body(jsonPayload)
                    .retrieve()
                    .toBodilessEntity();

            log.info(" Bắn Discord Webhook thành công tới user {}", partnerUserId);

        } catch (Exception e) {
            log.error(" Lỗi bắn Discord Webhook: {}", e.getMessage());
            throw new RuntimeException("Lỗi giao tiếp Webhook", e);
        }
    }

    private String calculateHmacSha256(String data, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey =
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
