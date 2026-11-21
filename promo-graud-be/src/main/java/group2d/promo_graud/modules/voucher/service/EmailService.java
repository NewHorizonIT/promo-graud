package group2d.promo_graud.modules.voucher.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.voucher.component.distribute.DistributionPayload;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String VOUCHER_EMAIL_TEMPLATE =
            """
            <div style="font-family: 'Segoe UI', Arial, sans-serif;
                max-width: 560px; margin: 0 auto; padding: 24px;
                background-color: #f8fafc;">
                <div style="background-color: #ffffff;
                    border-radius: 16px; padding: 24px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.05);">

                    <p style="margin: 0 0 6px; font-size: 13px;
                        font-weight: 700; color: #ea580c;
                        text-transform: uppercase; letter-spacing: 0.5px;">
                        🎉 Quà tặng dành riêng cho bạn
                    </p>

                    <h2 style="margin: 0 0 12px;
                        font-size: 20px; color: #0f172a;">
                        %s
                    </h2>

                    <p style="margin: 0 0 20px; font-size: 14px;
                        color: #475569; line-height: 1.5;">
                        Chúc mừng bạn đã nhận được mã ưu đãi từ
                        <strong>PromoGuard</strong>.
                        Hãy sử dụng mã bên dưới khi thanh toán
                        để nhận khuyến mãi nhé!
                    </p>

                    <!-- THẺ VOUCHER KIỂU CUỐNG VÉ -->
                    <div style="border: 2px dashed #f97316;
                        border-radius: 14px;
                        background: linear-gradient(
                            135deg, #fff7ed 0%%, #ffedd5 100%%);
                        overflow: hidden;">

                        <!-- Phần trên: Tên Rule & Mã Code -->
                        <div style="padding: 20px; text-align: center;">
                            <div style="font-size: 12px;
                                font-weight: 600; color: #9a3412;
                                text-transform: uppercase;
                                letter-spacing: 0.8px;">
                                %s
                            </div>

                            <div style="margin: 12px auto;
                                padding: 10px 20px;
                                background-color: #ffffff;
                                border: 1.5px solid #fdba74;
                                border-radius: 10px;
                                display: inline-block;">

                                <span style="font-family: 'Courier New',
                                    monospace; font-size: 22px;
                                    font-weight: 800; color: #ea580c;
                                    letter-spacing: 2px;">
                                    %s
                                </span>
                            </div>

                            <div style="font-size: 12px; color: #7c2d12;">
                                Sao chép mã trên và nhập tại bước
                                thanh toán
                            </div>
                        </div>

                        <!-- Đường cắt cuống vé -->
                        <div style="border-top: 2px dashed #fdba74;
                            margin: 0 14px;">
                        </div>

                        <!-- Phần dưới: Thời gian & Số lần sử dụng -->
                        <div style="padding: 14px 20px;
                            background-color: rgba(255, 255, 255, 0.65);
                            font-size: 13px; color: #334155;">

                            <table style="width: 100%%;
                                border-collapse: collapse;">
                                <tr>
                                    <td style="padding: 4px 0;
                                        color: #64748b;">
                                        ⏳ Bắt đầu:
                                    </td>
                                    <td style="padding: 4px 0;
                                        text-align: right;
                                        font-weight: 600;
                                        color: #0f172a;">
                                        %s
                                    </td>
                                </tr>

                                <tr>
                                    <td style="padding: 4px 0;
                                        color: #64748b;">
                                        ⌛ Hết hạn:
                                    </td>
                                    <td style="padding: 4px 0;
                                        text-align: right;
                                        font-weight: 600;
                                        color: #dc2626;">
                                        %s
                                    </td>
                                </tr>

                                <tr>
                                    <td style="padding: 4px 0;
                                        color: #64748b;">
                                        🎟️ Số lần sử dụng:
                                    </td>
                                    <td style="padding: 4px 0;
                                        text-align: right;
                                        font-weight: 600;
                                        color: #0f172a;">
                                        %d lượt
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>

                    <p style="margin: 20px 0 0; font-size: 12px;
                        color: #94a3b8; text-align: center;">
                        Email này được gửi tự động từ
                        Hệ thống PromoGuard.
                    </p>
                </div>
            </div>
            """;

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendVoucher(DistributionPayload payload) {
        String toEmail = payload.getUserEmail();

        // Bỏ qua gửi SMTP thật cho các tài khoản seed dạng test.
        if (toEmail == null || toEmail.trim().toLowerCase().startsWith("test_user")) {
            log.info("[SKIP SMTP] Bỏ qua email test ảo: {}", toEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(new InternetAddress(senderEmail, "Hệ thống PromoGuard", "UTF-8"));

            helper.setTo(toEmail);

            helper.setSubject(
                    "🎁 Bạn nhận được Voucher từ chiến dịch: "
                            + safeText(payload.getCampaignName(), "Ưu đãi đặc biệt"));

            int usageLimit = payload.getUsageLimit() != null ? payload.getUsageLimit() : 1;

            String htmlContent = buildVoucherEmailContent(payload, usageLimit);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            Thread.sleep(200);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Luồng gửi email bị ngắt", e);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email tới {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Gửi email thất bại", e);
        }
    }

    private String buildVoucherEmailContent(DistributionPayload payload, int usageLimit) {

        return VOUCHER_EMAIL_TEMPLATE.formatted(
                safeText(payload.getCampaignName(), "Chiến dịch khuyến mãi"),
                safeText(payload.getRuleName(), "Ưu đãi thành viên"),
                safeText(payload.getVoucherCode(), ""),
                safeText(payload.getStartTime(), "Có hiệu lực ngay"),
                safeText(payload.getEndTime(), "Không giới hạn"),
                usageLimit);
    }

    private String safeText(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
