package group2d.promo_graud.modules.voucher.component.distribute;

import java.time.format.DateTimeFormatter;
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

import group2d.promo_graud.modules.rules.RuleCampaign;
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
@StepScope
@RequiredArgsConstructor
public class DistributeVoucherWriter implements ItemWriter<Integer> {

    private static final String DEFAULT_WEBHOOK_URL = "DISCORD_DEMO";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final DistributionLogRepository logRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        List<Voucher> vouchers = getAvailableVouchers(chunkSize);

        Map<Integer, User> userMap = getUserMap(userIds);

        DistributionData data = prepareDistributionData(userIds, vouchers, userMap, chunkSize);

        saveDistributionData(data);

        publishDistributionEvent(data, userMap);
    }

    private List<Voucher> getAvailableVouchers(int chunkSize) {
        List<Voucher> vouchers =
                voucherRepository.findActiveVouchersWithLock(ruleId, channelStr, chunkSize);

        if (vouchers.size() < chunkSize) {
            log.error("Kho DB không đủ mã! Cần {}, có {}", chunkSize, vouchers.size());
            throw new RuntimeException("Không đủ mã voucher trong database");
        }

        return vouchers;
    }

    private Map<Integer, User> getUserMap(List<Integer> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    private DistributionData prepareDistributionData(
            List<Integer> userIds,
            List<Voucher> vouchers,
            Map<Integer, User> userMap,
            int chunkSize) {

        DistributionData data = new DistributionData();

        DistributionChannel channel = DistributionChannel.valueOf(channelStr);

        String finalWebhookUrl =
                (webhookUrl != null && !webhookUrl.isBlank()) ? webhookUrl : DEFAULT_WEBHOOK_URL;

        for (int i = 0; i < chunkSize; i++) {
            Integer userId = userIds.get(i);
            Voucher voucher = vouchers.get(i);

            processUser(userId, voucher, userMap, channel, finalWebhookUrl, data);
        }

        return data;
    }

    private void processUser(
            Integer userId,
            Voucher voucher,
            Map<Integer, User> userMap,
            DistributionChannel channel,
            String finalWebhookUrl,
            DistributionData data) {

        try {
            User targetUser = userMap.get(userId);

            if (targetUser == null) {
                voucher.setStatus(VoucherStatus.ACTIVE);
                return;
            }

            data.getValidUserIds().add(userId);
            data.getValidVouchers().add(voucher);

            data.getVoucherIdsToUpdate().add(voucher.getId());

            UserVoucher userVoucher =
                    UserVoucher.builder()
                            .user(User.builder().id(userId).build())
                            .voucher(Voucher.builder().id(voucher.getId()).build())
                            .status(UserVoucherStatus.UNUSED)
                            .build();

            data.getUserVouchers().add(userVoucher);

            String userEmail = targetUser.getEmail();

            String recipientValue =
                    (channel == DistributionChannel.EMAIL) ? userEmail : finalWebhookUrl;

            DistributionLog distributionLog =
                    DistributionLog.builder()
                            .voucher(Voucher.builder().id(voucher.getId()).build())
                            .channel(channel)
                            .recipient(recipientValue)
                            .status(DistributionLogStatus.PENDING)
                            .retryCount(0)
                            .build();

            data.getLogs().add(distributionLog);

        } catch (Exception ex) {
            log.error("Lỗi khi chuẩn bị data cho User ID {}: {}", userId, ex.getMessage());

            voucher.setStatus(VoucherStatus.ACTIVE);
        }
    }

    private void saveDistributionData(DistributionData data) {
        if (data.getVoucherIdsToUpdate().isEmpty()) {
            return;
        }

        voucherRepository.updateStatus(data.getVoucherIdsToUpdate());
        userVoucherRepository.saveAll(data.getUserVouchers());
        logRepository.saveAllAndFlush(data.getLogs());
    }

    private void publishDistributionEvent(DistributionData data, Map<Integer, User> userMap) {

        List<DistributionPayload> payloads = buildPayloads(data, userMap);

        if (!payloads.isEmpty()) {
            eventPublisher.publishEvent(new VoucherDistributionEvent(payloads));
        }
    }

    private List<DistributionPayload> buildPayloads(
            DistributionData data, Map<Integer, User> userMap) {

        List<DistributionPayload> payloads = new ArrayList<>();

        if (data.getLogs().isEmpty()) {
            return payloads;
        }

        RuleCampaign rule = data.getValidVouchers().getFirst().getRuleCampaign();

        String campaignName = rule.getCampaign().getName();
        String ruleName = rule.getName();

        String startTimeStr =
                rule.getStartTime() != null
                        ? rule.getStartTime().format(DATE_FORMATTER)
                        : "Có hiệu lực ngay";

        String endTimeStr =
                rule.getEndTime() != null
                        ? rule.getEndTime().format(DATE_FORMATTER)
                        : "Không giới hạn";

        for (int i = 0; i < data.getLogs().size(); i++) {
            payloads.add(
                    buildPayload(
                            data, userMap, i, campaignName, ruleName, startTimeStr, endTimeStr));
        }

        return payloads;
    }

    private DistributionPayload buildPayload(
            DistributionData data,
            Map<Integer, User> userMap,
            int index,
            String campaignName,
            String ruleName,
            String startTimeStr,
            String endTimeStr) {

        Integer userId = data.getValidUserIds().get(index);
        Voucher voucher = data.getValidVouchers().get(index);
        Integer logId = data.getLogs().get(index).getId();
        String userEmail = userMap.get(userId).getEmail();

        String finalWebhookUrl =
                (webhookUrl != null && !webhookUrl.isBlank()) ? webhookUrl : DEFAULT_WEBHOOK_URL;

        DistributionChannel channel = DistributionChannel.valueOf(channelStr);

        return new DistributionPayload(
                logId,
                userEmail,
                voucher.getCode(),
                channel,
                finalWebhookUrl,
                campaignName,
                ruleName,
                startTimeStr,
                endTimeStr,
                voucher.getLimitClient());
    }

    private static class DistributionData {

        private final List<UserVoucher> userVouchers = new ArrayList<>();
        private final List<Integer> voucherIdsToUpdate = new ArrayList<>();
        private final List<DistributionLog> logs = new ArrayList<>();
        private final List<Integer> validUserIds = new ArrayList<>();
        private final List<Voucher> validVouchers = new ArrayList<>();

        public List<UserVoucher> getUserVouchers() {
            return userVouchers;
        }

        public List<Integer> getVoucherIdsToUpdate() {
            return voucherIdsToUpdate;
        }

        public List<DistributionLog> getLogs() {
            return logs;
        }

        public List<Integer> getValidUserIds() {
            return validUserIds;
        }

        public List<Voucher> getValidVouchers() {
            return validVouchers;
        }
    }
}
