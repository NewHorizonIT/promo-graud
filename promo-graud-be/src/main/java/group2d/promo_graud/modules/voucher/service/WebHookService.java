package group2d.promo_graud.modules.voucher.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.voucher.component.distribute.DistributionPayload;

@RequiredArgsConstructor
@Slf4j
@Service
public class WebHookService {

    private static final String WEBHOOK_JSON_TEMPLATE =
            """
            {
              "username": "PromoGuard B2B Gateway",
              "avatar_url": "https://cdn-icons-png.flaticon.com/512/9422/9422820.png",
              "embeds": [
                {
                  "author": {
                    "name": "PROMOGUARD ENTERPRISE • DISTRIBUTION EVENT",
                    "icon_url": "https://cdn-icons-png.flaticon.com/512/2038/2038768.png"
                  },
                  "title": "🎟️ CẤP PHÁT VOUCHER THÀNH CÔNG",
                  "description": "Hệ thống vừa kích hoạt và bàn giao mã ưu đãi thuộc chiến dịch **%s**.",
                  "color": 1096065,
                  "thumbnail": {
                    "url": "https://cdn-icons-png.flaticon.com/512/3837/3837379.png"
                  },
                  "fields": [
                    {
                      "name": "🏷️ Mã Voucher",
                      "value": "```%s```",
                      "inline": false
                    },
                    {
                      "name": "📌 Chương trình áp dụng",
                      "value": "**%s**",
                      "inline": true
                    },
                    {
                      "name": "👤 Người nhận",
                      "value": "`%s`",
                      "inline": true
                    },
                    {
                      "name": "🔁 Lượt sử dụng",
                      "value": "`%d lượt / khách`",
                      "inline": true
                    },
                    {
                      "name": "⏳ Hiệu lực từ",
                      "value": "%s",
                      "inline": true
                    },
                    {
                      "name": "⌛ Hết hạn lúc",
                      "value": "%s",
                      "inline": true
                    },
                    {
                      "name": "🛡️ Đối soát & Bảo mật",
                      "value": "`ALLOCATED` • Log `#%d` • `HMAC-SHA256`",
                      "inline": false
                    }
                  ],
                  "footer": {
                    "text": "PromoGuard Engine • B2B Webhook Delivery",
                    "icon_url": "https://cdn-icons-png.flaticon.com/512/9422/9422820.png"
                  },
                  "timestamp": "%s"
                }
              ]
            }
            """;

    private final RestClient restClient;

    @Value("${promo-graud.webhook.discord-url}")
    private String defaultDiscordUrl;

    @Value("${promo-graud.webhook.secret-key}")
    private String secretKey;

    public void sendVoucherWebHook(DistributionPayload payload) {
        String partnerUrl = payload.getTargetUrl();

        String targetUrl =
                (partnerUrl == null || partnerUrl.isBlank() || partnerUrl.equals("DISCORD_DEMO"))
                        ? defaultDiscordUrl
                        : partnerUrl;

        int usageLimit = payload.getUsageLimit() != null ? payload.getUsageLimit() : 1;

        int logId = payload.getLogId() != null ? payload.getLogId() : 0;

        String recipient = escapeJson(safeText(payload.getUserEmail(), "Khách hàng hệ thống"));

        String nowIso = Instant.now().toString();

        try {
            String jsonPayload = buildWebhookPayload(payload, recipient, usageLimit, logId, nowIso);

            String signature = calculateHmacSha256(jsonPayload, secretKey);

            restClient
                    .post()
                    .uri(targetUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-PromoGuard-Signature", signature)
                    .header("X-PromoGuard-Event", "voucher.allocated")
                    .body(jsonPayload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Bắn Webhook thành công [Log #{}] tới {}", logId, recipient);

        } catch (Exception e) {
            log.error("Lỗi bắn Webhook [Log #{}]: {}", logId, e.getMessage());
            throw new RuntimeException("Lỗi giao tiếp Webhook", e);
        }
    }

    private String buildWebhookPayload(
            DistributionPayload payload,
            String recipient,
            int usageLimit,
            int logId,
            String nowIso) {

        String campaignName =
                escapeJson(safeText(payload.getCampaignName(), "Chiến dịch khuyến mãi"));

        String ruleName = escapeJson(safeText(payload.getRuleName(), "Ưu đãi thành viên"));

        String voucherCode = escapeJson(safeText(payload.getVoucherCode(), "N/A"));

        String startTime = escapeJson(safeText(payload.getStartTime(), "Có hiệu lực ngay"));

        String endTime = escapeJson(safeText(payload.getEndTime(), "Không giới hạn"));

        return WEBHOOK_JSON_TEMPLATE.formatted(
                campaignName,
                voucherCode,
                ruleName,
                recipient,
                usageLimit,
                startTime,
                endTime,
                logId,
                nowIso);
    }

    private String safeText(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private String calculateHmacSha256(String data, String secret) throws Exception {

        Mac sha256Hmac = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKeySpec =
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        sha256Hmac.init(secretKeySpec);

        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(hash);
    }
}
