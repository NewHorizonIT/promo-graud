package group2d.promo_graud.modules.voucher.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.modules.voucher.dto.requests.DistributeVoucherRequest;
import group2d.promo_graud.modules.voucher.dto.responses.DistributionResponse;
import group2d.promo_graud.modules.voucher.entity.DistributionLog;
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

    // Nếu kho hết mã, sẽ quăng lỗi ngay lập tức về Frontend mà không cần gọi Spring Batch.
    public Long distribute(DistributeVoucherRequest request) {
        List<Integer> finalUserIds = request.getUserIds();

        // Nếu Admin chọn "Gửi cho tất cả hạng VÀNG/BẠC..."
        if (request.getTargetUserType() != null) {
            // Có chọn loại user và có chọn danh sách user cụ thể
            if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
                finalUserIds =
                        userRepository.findIdsByTypeAndIdIn(
                                request.getTargetUserType(), request.getUserIds());

            } else {
                // Chỉ chọn loại user => gửi cho tất cả user thuộc loại đó
                finalUserIds = userRepository.findIdsByType(request.getTargetUserType());
            }

        } else {
            // Không chọn hạng -> gửi user thủ công
            finalUserIds =
                    request.getUserIds() == null
                            ? List.of()
                            : request.getUserIds().stream().distinct().toList();
        }

        // Chặn lỗi nếu danh sách rỗng (Không có user nào để gửi)
        if (finalUserIds == null || finalUserIds.isEmpty()) {
            throw new AppException(VoucherErrorCode.NO_USER_SELECTED);
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
        return distributeBatchService.launchDistributionJob(request);
    }

    public Page<DistributionResponse> getAllDistribution(
            String recipient,
            DistributionLogStatus status,
            Integer retryCount,
            DistributionChannel channel,
            int size,
            int page) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        String keyword = recipient == null ? "" : recipient.trim();
        return distributionLogRepository
                .getAll(keyword, status, retryCount, channel, pageable)
                .map(this::mapToResponse);
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
