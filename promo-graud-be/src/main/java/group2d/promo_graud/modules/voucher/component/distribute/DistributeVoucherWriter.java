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

    @Value("#{jobParameters['targetUrl']}")
    private String webhookUrl;

    @Override
    public void write(Chunk<? extends Integer> chunk) throws Exception {
        List<Integer> userIds = (List<Integer>) chunk.getItems();
        int chunkSize = userIds.size();
        List<Voucher> vouchers =
                voucherRepository.findActiveVouchersWithLock(ruleId, channelStr, chunkSize);
        if (vouchers.size() < chunkSize) {
            log.error("Kho DB không đủ mã! Cần {}, có {}", chunkSize, vouchers.size());
            throw new RuntimeException("Không đủ mã voucher trong database");
        }
        Map<Integer, User> userMap =
                userRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(User::getId, user -> user));
        List<UserVoucher> userVouchers = new ArrayList<>();
        List<Integer> voucherIdsToUpdate = new ArrayList<>();
        List<DistributionLog> logs = new ArrayList<>();
        // TẠO THÊM 2 DANH SÁCH ĐỂ LƯU LẠI NHỮNG NGƯỜI HỢP LỆ
        List<Integer> validUserIds = new ArrayList<>();
        List<Voucher> validVouchers = new ArrayList<>();
        DistributionChannel channel = DistributionChannel.valueOf(channelStr);
        String finalWebhookUrl =
                (webhookUrl != null && !webhookUrl.isBlank()) ? webhookUrl : "DISCORD_DEMO";
        for (int i = 0; i < chunkSize; i++) {
            Integer userId = userIds.get(i);
            Voucher voucher = vouchers.get(i);
            try {
                User targetUser = userMap.get(userId);
                if (targetUser == null) {
                    voucher.setStatus(VoucherStatus.ACTIVE);
                    continue;
                }
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
                String userEmail = userMap.get(userId).getEmail(); // Lấy Email của User từ Database
                String recipientValue =
                        (channel == DistributionChannel.EMAIL) ? userEmail : finalWebhookUrl;
                DistributionLog log =
                        DistributionLog.builder()
                                .voucher(Voucher.builder().id(voucher.getId()).build())
                                .channel(channel)
                                .recipient(recipientValue) // Lưu String (Email hoặc URL)
                                .status(DistributionLogStatus.PENDING)
                                .retryCount(0)
                                .build();
                logs.add(log);
            } catch (Exception ex) {
                log.error("Lỗi khi chuẩn bị data cho User ID {}: {}", userId, ex.getMessage());
                voucher.setStatus(VoucherStatus.ACTIVE);
            }
        }
        if (!voucherIdsToUpdate.isEmpty()) {
            voucherRepository.updateStatus(voucherIdsToUpdate);
            userVoucherRepository.saveAll(userVouchers);
            logRepository.saveAllAndFlush(logs);
        }
        // Khởi tạo Payload từ dữ liệu đã lưu và gửi sang Redis
        List<DistributionPayload> payloads = new ArrayList<>();
        for (int i = 0; i < logs.size(); i++) {
            Integer userId = validUserIds.get(i);
            Voucher voucher = validVouchers.get(i);
            Integer logId = logs.get(i).getId();
            String userEmail = userMap.get(userId).getEmail();
            payloads.add(
                    new DistributionPayload(
                            logId, userEmail, voucher.getCode(), channel, finalWebhookUrl));
        }
        if (!payloads.isEmpty()) {
            eventPublisher.publishEvent(new VoucherDistributionEvent(payloads));
        }
    }
}
