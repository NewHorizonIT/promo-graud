package group2d.promo_graud.modules.voucher.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.modules.voucher.dto.requests.DistributeVoucherRequest;
import group2d.promo_graud.modules.voucher.enums.VoucherErrorCode;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;
import group2d.promo_graud.modules.voucher.repository.VoucherRepository;
import group2d.promo_graud.shared.exception.AppException;

@Service
@RequiredArgsConstructor
public class DistributeVoucherService {
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final VoucherRepository voucherRepository;
    private final DistributeBatchService distributeBatchService;

    // Nếu kho hết mã, sẽ quăng lỗi ngay lập tức về Frontend mà không cần gọi Spring Batch.
    public Long distribute(DistributeVoucherRequest request) {
        List<Integer> finalUserIds = request.getUserIds();

        // Nếu Admin chọn "Gửi cho tất cả hạng VÀNG/BẠC..."
        if (request.getTargetUserType() != null) {
            // Cần viết thêm hàm này trong UserRepository để moi ra danh sách ID
            finalUserIds = userRepository.findIdsByType(request.getTargetUserType());

            // Gán ngược lại vào request để đẩy sang Batch
            request.setUserIds(finalUserIds);
        }

        // Chặn lỗi nếu danh sách rỗng (Không có user nào để gửi)
        if (finalUserIds == null || finalUserIds.isEmpty()) {
            throw new AppException(VoucherErrorCode.NO_USER_SELECTED);
        }
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
                    voucherRepository.countByRuleCampaign_IdAndTypeAndStatusAndDistributionChannel(
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
}
