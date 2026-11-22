package group2d.promo_graud.modules.voucher.service;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.RuleCampaignRepository;
import group2d.promo_graud.modules.rules.enums.RuleCampaignErrorCode;
import group2d.promo_graud.modules.rules.enums.RuleStatus;
import group2d.promo_graud.modules.rules.enums.RuleTargetAudience;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.modules.voucher.component.distribute.DistributionPayload;
import group2d.promo_graud.modules.voucher.component.worker.VoucherDistributionEvent;
import group2d.promo_graud.modules.voucher.dto.requests.DistributeVoucherRequest;
import group2d.promo_graud.modules.voucher.dto.responses.DistributionResponse;
import group2d.promo_graud.modules.voucher.entity.DistributionLog;
import group2d.promo_graud.modules.voucher.entity.Voucher;
import group2d.promo_graud.modules.voucher.enums.*;
import group2d.promo_graud.modules.voucher.repository.DistributionLogRepository;
import group2d.promo_graud.modules.voucher.repository.VoucherRepository;
import group2d.promo_graud.shared.exception.AppException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributeVoucherService {
    private final UserRepository userRepository;
    private final DistributionLogRepository distributionLogRepository;
    private final StringRedisTemplate redisTemplate;
    private final VoucherRepository voucherRepository;
    private final DistributeBatchService distributeBatchService;
    private final RuleCampaignRepository ruleCampaignRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Nếu kho hết mã, sẽ quăng lỗi ngay lập tức về Frontend mà không cần gọi Spring Batch.
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Long distribute(DistributeVoucherRequest request) {
        RuleCampaign ruleCampaign =
                ruleCampaignRepository
                        .findById(request.getRuleId())
                        .orElseThrow(() -> new AppException(RuleCampaignErrorCode.RULE_NOT_EXIST));

        if (ruleCampaign.getStatus() == RuleStatus.INACTIVE) {
            throw new AppException(RuleCampaignErrorCode.RULE_NOT_ELIGIBLE);
        }

        List<Integer> finalUserIds = resolveUserIds(request);

        // Chặn lỗi nếu danh sách rỗng (Không có user nào để gửi)
        if (finalUserIds.isEmpty()) {
            throw new AppException(VoucherErrorCode.NO_USER_SELECTED);
        }
        // Check nghiệp vụ: Nếu Rule giới hạn hạng user (vd: VIP), toàn bộ user được chọn phải thuộc
        // hạng đó
        RuleTargetAudience audience = ruleCampaign.getTypeOfUser();
        if (audience != null && audience != RuleTargetAudience.ALL) {
            UserTypeEnum requiredType = UserTypeEnum.valueOf(audience.name());
            List<Integer> validUserIds =
                    userRepository.findIdsByTypeAndIdIn(requiredType, finalUserIds);
            if (validUserIds.size() < finalUserIds.size()) {
                throw new AppException(RuleCampaignErrorCode.USER_TYPE_NOT_MATCH_RULE);
            }
        }
        // Gán ngược lại vào request để đẩy sang Batch
        request.setUserIds(finalUserIds);
        // đếm số lượng user cần gửi
        int targetUserCount = finalUserIds.size();
        String redisKey =
                "voucher_count:rule_"
                        + request.getRuleId()
                        + ":channel_"
                        + request.getDistributionChannel().name();

        // Lazy-load: Nếu Redis chưa có thì đếm DB nạp lên
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            Long countInDb =
                    voucherRepository.countByRuleCampaignIdAndTypeAndStatusAndDistributionChannel(
                            request.getRuleId(),
                            VoucherType.UNIQUE,
                            VoucherStatus.ACTIVE,
                            request.getDistributionChannel());
            // Set giá trị vào Redis kèm TTL là 2h để dọn rác bộ nhớ
            redisTemplate.opsForValue().set(redisKey, String.valueOf(countInDb), 2, TimeUnit.HOURS);
        }

        // FAST-FAIL: Trừ trực tiếp số lượng người cần phát trên Redis
        Long remainVoucher = redisTemplate.opsForValue().decrement(redisKey, targetUserCount);

        if (remainVoucher != null && remainVoucher < 0) {
            // kho không đủ mã -> hoàn trả lại số vừa trừ
            redisTemplate.opsForValue().increment(redisKey, targetUserCount);

            throw new AppException(VoucherErrorCode.INSUFFICIENT_QUANTITY);
        }

        // Đủ hàng -> kích hoạt spring batch
        // Gọi sang Batch Service để chạy Job
        try {
            return distributeBatchService.launchDistributionJob(request);
        } catch (Exception e) {
            redisTemplate.opsForValue().increment(redisKey, targetUserCount);
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Page<DistributionResponse> getAllDistribution(
            String recipient,
            DistributionLogStatus status,
            DistributionChannel channel,
            int size,
            int page) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        String keyword = recipient == null ? "" : recipient.trim();
        return distributionLogRepository
                .getAll(keyword, status, channel, pageable)
                .map(this::mapToResponse);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public DistributionResponse getDistributionById(Integer id) {
        DistributionLog distributionLog =
                distributionLogRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new AppException(
                                                DistributionErrorCode.DISTRIBUTION_NOT_FOUND));
        return mapToResponse(distributionLog);
    }

    private List<Integer> resolveUserIds(DistributeVoucherRequest request) {
        List<Integer> userIds = request.getUserIds() == null ? List.of() : request.getUserIds();
        UserTypeEnum type = request.getTargetUserType();
        List<Integer> result;

        if (Boolean.TRUE.equals(request.getSelectAll())) {
            if (type != null) {
                result = userRepository.findIdsByType(type);
            } else {
                result = userRepository.findByIsDeletedFalse().stream().map(User::getId).toList();
            }

            List<Integer> excludedList = request.getExcludedUserIds();
            if (excludedList != null && !excludedList.isEmpty()) {
                Set<Integer> excluded = new HashSet<>(excludedList);
                result = result.stream().filter(id -> !excluded.contains(id)).toList();
            }
        } else if (type != null) {
            result =
                    userIds.isEmpty()
                            ? userRepository.findIdsByType(type)
                            : userRepository.findIdsByTypeAndIdIn(type, userIds);
        } else {
            result = userIds;
        }

        return result.stream().distinct().toList();
    }

    // distribute voucher generic webhook
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void broadcastGenericWebhook(Integer voucherId) {
        Voucher voucher =
                voucherRepository
                        .findById(voucherId)
                        .orElseThrow(() -> new AppException(VoucherErrorCode.NOT_EXIST));

        if (voucher.getType() != VoucherType.GENERIC
                || voucher.getDistributionChannel() != DistributionChannel.WEBHOOK) {
            throw new AppException(VoucherErrorCode.VOUCHER_TYPE_INVALID);
        }

        RuleCampaign rule = voucher.getRuleCampaign();
        if (voucher.getStatus() != VoucherStatus.ACTIVE || rule.getStatus() != RuleStatus.ACTIVE) {
            throw new AppException(RuleCampaignErrorCode.RULE_NOT_ELIGIBLE);
        }

        RuleTargetAudience audience = rule.getTypeOfUser();
        String targetAudienceLabel =
                (audience == null || audience == RuleTargetAudience.ALL)
                        ? "Toàn bộ khách hàng (GENERIC)"
                        : "Khách hàng hạng " + audience.name() + " (GENERIC)";

        // 1. Ghi log đối soát
        String recipientLabel = "DISCORD_DEMO";
        DistributionLog logEntry =
                DistributionLog.builder()
                        .voucher(voucher)
                        .channel(DistributionChannel.WEBHOOK)
                        .recipient(recipientLabel)
                        .status(DistributionLogStatus.PENDING)
                        .retryCount(0)
                        .build();
        distributionLogRepository.saveAndFlush(logEntry);

        // 2. Đóng gói payload gửi sang Redis Stream -> Discord Webhook
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String startTimeStr =
                rule.getStartTime() != null
                        ? rule.getStartTime().format(formatter)
                        : "Có hiệu lực ngay";
        String endTimeStr =
                rule.getEndTime() != null ? rule.getEndTime().format(formatter) : "Không giới hạn";

        DistributionPayload payload =
                new DistributionPayload(
                        logEntry.getId(),
                        targetAudienceLabel, // Sẽ hiện: "Khách hàng hạng SILVER (GENERIC)"
                        voucher.getCode(),
                        DistributionChannel.WEBHOOK,
                        "DISCORD_DEMO",
                        rule.getCampaign().getName(),
                        rule.getName(),
                        startTimeStr,
                        endTimeStr,
                        voucher.getLimitClient());

        eventPublisher.publishEvent(new VoucherDistributionEvent(List.of(payload)));
    }

    private DistributionResponse mapToResponse(DistributionLog distributionLog) {

        return DistributionResponse.builder()
                .id(distributionLog.getId())
                .voucherId(distributionLog.getVoucher().getId())
                .voucherCode(distributionLog.getVoucher().getCode())
                .type(distributionLog.getVoucher().getType())
                .channel(distributionLog.getChannel())
                .errorMessage(distributionLog.getErrorMessage())
                .status(distributionLog.getStatus())
                .lastAttemptAt(distributionLog.getLastAttemptAt())
                .recipient(distributionLog.getRecipient())
                .retryCount(distributionLog.getRetryCount())
                .build();
    }
}
