package group2d.promo_graud.modules.voucher.service;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("dev") // Chỉ khởi tạo Bean này nếu ứng dụng chạy ở chế độ DEV
public class DevEmailService {
    private final JavaMailSender mailSender;

    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendVoucher(String toEmail, String voucherCode) {
        // Giả lập gửi email, không gọi Mailtrap hay SMTP
        log.info(
                "[MOCK EMAIL] To: {}"
                        + "\nChúc mừng! Bạn nhận được Voucher mới"
                        + " Mã Voucher của bạn là: {}"
                        + "\nHãy sử dụng ngay hôm nay nhé!",
                toEmail,
                voucherCode);
        //    SimpleMailMessage message = new SimpleMailMessage();
        //
        //    message.setTo(toEmail);
        //    message.setSubject("Chúc mừng! Bạn nhận được Voucher mới");
        //    message.setText("Mã Voucher của bạn là: " + voucherCode + "\nHãy sử dụng ngay hôm nay
        // nhé!");
        //
        //    mailSender.send(message);
    }
}
