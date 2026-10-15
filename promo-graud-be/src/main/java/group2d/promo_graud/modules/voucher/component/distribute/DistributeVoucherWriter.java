package group2d.promo_graud.modules.voucher.component.distribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.entity.UserVoucher;
import group2d.promo_graud.modules.user.enums.UserVoucherStatus;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.modules.user.repository.UserVoucherRepository;
import group2d.promo_graud.modules.voucher.component.worker.VoucherDistributionEvent;
import group2d.promo_graud.modules.voucher.entity.DistributionLog;
import group2d.promo_graud.modules.voucher.entity.Voucher;
import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.DistributionLogStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.repository.DistributionLogRepository;
import group2d.promo_graud.modules.voucher.repository.VoucherRepository;

@Component
@Slf4j
@StepScope // BẮT BUỘC CÓ để dùng được @Value lấy jobParameters
@RequiredArgsConstructor
public class DistributeVoucherWriter implements ItemWriter<Integer> {
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final DistributionLogRepository logRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Lấy thông tin từ lúc khởi chạy Job
    @Value("#{jobParameters['ruleId']}")
    private Integer ruleId;

    @Value("#{jobParameters['channel']}")
    private String channelStr;

    @Override
    public void write(Chunk<? extends Integer> chunk) throws Exception {
        // chunk.getItem trả về List các UserId
        List<Integer> userIds = (List<Integer>) chunk.getItems();
        int chunkSize = userIds.size();
        // 1. Rút MỘT LẦN đủ N mã voucher từ Database (Giải quyết triệt để lỗi trùng mã)
        List<Voucher> vouchers =
                voucherRepository.findActiveVouchersWithLock(ruleId, channelStr, chunkSize);

        if (vouchers.size() < chunkSize) {
            log.error("Kho DB không đủ mã! Cần {}, có {}", chunkSize, vouchers.size());
            throw new RuntimeException("Không đủ mã voucher trong database");
        }

        // Lấy danh sách User và chuyển thành Map (UserId -> User) để tìm kiếm tốc độ O(1)
        Map<Integer, User> userMap =
                userRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

        // Tạm thời hard-code URL test ở đây. Sau này bạn lấy từ currentRule.getPayload() ra nhé.
        String webhookUrl = "https://webhook.site/f37d210e-d515-4821-9b6a-218b1cf2feeb";

        List<UserVoucher> userVouchers = new ArrayList<>();
        List<Integer> voucherIdsToUpdate = new ArrayList<>();
        List<DistributionLog> logs = new ArrayList<>();

        // TẠO THÊM 2 DANH SÁCH ĐỂ LƯU LẠI NHỮNG NGƯỜI HỢP LỆ
        List<Integer> validUserIds = new ArrayList<>();
        List<Voucher> validVouchers = new ArrayList<>();
        DistributionChannel channel = DistributionChannel.valueOf(channelStr);
        for (int i = 0; i < chunkSize; i++) {
            Integer userId = userIds.get(i);
            Voucher voucher = vouchers.get(i);

            User targetUser = userMap.get(userId);

            // NẾU USER KHÔNG TỒN TẠI -> BỎ QUA
            if (targetUser == null) {
                log.warn("Bỏ qua User ID {} vì không tìm thấy trong Database", userId);
                voucher.setStatus(VoucherStatus.ACTIVE);
                continue;
            }
            // LƯU LẠI VÀO DANH SÁCH HỢP LỆ ĐỂ LÁT NỮA XÀI
            validUserIds.add(userId);
            validVouchers.add(voucher);

            // 1. Chuẩn bị list Voucher ID để đổi trạng thái
            voucherIdsToUpdate.add(voucher.getId());

            // 2. Chuẩn bị data lưu bảng user_voucher
            UserVoucher uv =
                    UserVoucher.builder()
                            .user(User.builder().id(userId).build())
                            .voucher(Voucher.builder().id(voucher.getId()).build())
                            .status(UserVoucherStatus.UNUSED)
                            .build();
            userVouchers.add(uv);

            // 3. Chuẩn bị data lưu bảng distribution_log (recipient là userId)
            String userEmail = userMap.get(userId).getEmail(); // Lấy Email của User từ Database
            // XÁC ĐỊNH LƯU GÌ VÀO CỘT RECIPIENT
            String recipientValue = (channel == DistributionChannel.EMAIL) ? userEmail : webhookUrl;

            DistributionLog log =
                    DistributionLog.builder()
                            .voucher(Voucher.builder().id(voucher.getId()).build())
                            .channel(channel)
                            .recipient(recipientValue) // Lưu String (Email hoặc URL)
                            .status(DistributionLogStatus.PENDING)
                            .retryCount(0)
                            .build();
            logs.add(log);
        }
        // CHẶN LỖI: Chỉ cập nhật DB nếu có ít nhất 1 user hợp lệ
        if (!voucherIdsToUpdate.isEmpty()) {
            voucherRepository.updateStatus(voucherIdsToUpdate);
            userVoucherRepository.saveAll(userVouchers);
            logRepository.saveAllAndFlush(logs);
        }
        // Thực thi xuống Database (Tối ưu bằng cách lưu hàng loạt)
        voucherRepository.updateStatus(voucherIdsToUpdate);
        // Tối ưu: Lưu hàng loạt thay vì lưu từng dòng
        userVoucherRepository.saveAll(userVouchers);
        logRepository.saveAllAndFlush(logs);

        // Khởi tạo Payload từ dữ liệu đã lưu và gửi sang Redis
        List<DistributionPayload> payloads = new ArrayList<>();
        for (int i = 0; i < logs.size(); i++) {
            Integer userId = validUserIds.get(i);
            Voucher voucher = validVouchers.get(i);
            Integer logId = logs.get(i).getId();
            String userEmail = userMap.get(userId).getEmail();

            payloads.add(
                    new DistributionPayload(
                            logId, userEmail, voucher.getCode(), channel, webhookUrl));
        }

        // Spring sẽ tự động hiểu là "Hãy phát sự kiện này đi, nhưng nhớ chờ Transaction hiện tại
        // chốt xong DB đã nhé".
        if (!payloads.isEmpty()) {
            eventPublisher.publishEvent(new VoucherDistributionEvent(payloads));
        }
    }
}
