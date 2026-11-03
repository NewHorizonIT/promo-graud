package group2d.promo_graud.modules.voucher.service;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("dev") // Chỉ khởi tạo Bean này nếu ứng dụng chạy ở chế độ DEV
public class DevEmailService {
    private final JavaMailSender mailSender;

    public void sendVoucher(String toEmail, String voucherCode) {
        log.info("Chuẩn bị gửi email voucher [{}] tới địa chỉ: {}", voucherCode, toEmail);

        try {
            // SimpleMailMessage message = new SimpleMailMessage();
            // message.setFrom("Hệ thống PromoGuard <no-reply@promoguard.com>");
            // message.setTo(toEmail);
            // message.setSubject("Chúc mừng! Bạn nhận được Voucher mới");
            // message.setText("Mã Voucher của bạn là: " + voucherCode + "\nHãy sử dụng ngay hôm nay
            // nhé!");
            //
            // mailSender.send(message);

            log.info("Đã gửi email voucher thành công tới {}", toEmail);

        } catch (Exception e) {
            // Log chi tiết nguyên nhân lỗi gửi mail
            log.error("Lỗi khi gửi email tới {}: {}", toEmail, e.getMessage());

            // QUAN TRỌNG: Quăng ngược lỗi ra ngoài để Worker bắt được và KHÔNG gọi lệnh XACK.
            // Nhờ đó, tin nhắn mới kẹt lại trong PEL để Cronjob vớt lên gửi lại sau.
            throw new RuntimeException("Gửi email thất bại", e);
        }
    }
}
