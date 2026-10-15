package group2d.promo_graud.modules.voucher.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("prod") // Chỉ khởi tạo Bean này nếu ứng dụng chạy ở chế độ PROD
public class ProdEmailService {
    @Value("${resend.api-key}")
    private String apiKey;

    @Bean
    public Resend resend() {
        return new Resend(apiKey);
    }

    public void sendVoucher(String toEmail, String voucherCode) {
        // Khởi tạo client của Resend
        Resend resends = resend();

        // Thiết kế nội dung Email bằng HTML
        String htmlContent =
                String.format(
                        "<h2>Chúc mừng bạn!</h2>"
                                + "<p>Bạn vừa nhận được một Voucher mua sắm ưu đãi.</p>"
                                + "<p>Mã của bạn là: <strong style='color: red; font-size: 20px;'>%s</strong></p>"
                                + "<p>Hãy sử dụng ngay hôm nay nhé!</p>",
                        voucherCode);

        // Đóng gói Payload theo chuẩn Resend
        CreateEmailOptions params =
                CreateEmailOptions.builder()
                        .from("Acme <onboarding@resend.dev>")
                        .to(toEmail)
                        .subject("Chúc mừng! Bạn nhận được Voucher mới")
                        .html(htmlContent)
                        .build();

        try {
            // Gọi API gửi mail với tốc độ ánh sáng
            CreateEmailResponse data = resends.emails().send(params);
            log.info("Gửi email thành công qua Resend! ID: {}", data.getId());
        } catch (ResendException e) {
            log.error("Lỗi khi gửi email qua Resend: {}", e.getMessage());
            // Ném lỗi ra ngoài để Worker bắt được và update Database thành FAILED
            throw new RuntimeException("Resend API Error", e);
        }
    }
}
