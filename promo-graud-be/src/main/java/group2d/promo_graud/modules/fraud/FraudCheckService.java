package group2d.promo_graud.modules.fraud;

import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.VoucherRedemptionRepository;
import group2d.promo_graud.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FraudCheckService {

  private static final List<OrderStatus> COUNTED = List.of(OrderStatus.SUCCESS);

  private final VoucherRedemptionRepository voucherRedemptionRepository;
  private final FraudLogService fraudLogService;

  @Value("${fraud.max-redemptions-per-ip-24h:3}")
  private int maxPerIp24h;

  @Value("${fraud.max-users-per-device-24h:3}")
  private int maxUsersPerDevice24h;

  /**
   * Gọi trong transaction tạo đơn, sau khi đã lấy lock (user, voucher) và (thiết bị, voucher).
   * Ném FraudDetectedException nếu phát hiện gian lận.
   */
  public void check(Integer userId, Voucher voucher, String ip, String fingerprint) {
    if (voucherRedemptionRepository.countByUser(userId, voucher.getId(), COUNTED)
      >= voucher.getLimitClient()) {
      throw new FraudDetectedException(FraudReason.USER_REUSED);
    }
    if (voucherRedemptionRepository.countByDevice(voucher.getId(), fingerprint, COUNTED)
      >= voucher.getLimitClient()) {
      throw new FraudDetectedException(FraudReason.DEVICE_REUSED);
    }

    LocalDateTime since = LocalDateTime.now().minusHours(24);
    if (voucherRedemptionRepository.countOtherUsersOnDevice(fingerprint, userId, since)
      >= maxUsersPerDevice24h) {
      throw new FraudDetectedException(FraudReason.MULTI_ACCOUNT_DEVICE);
    }
    if (voucherRedemptionRepository.countByIpSince(ip, COUNTED, since) >= maxPerIp24h) {
      throw new FraudDetectedException(FraudReason.IP_LIMIT_EXCEEDED);
    }
  }

  /**
   * Gọi trong khối catch của order (NGOÀI transaction): ghi fraud_log async rồi trả
   * AppException để order ném ra. Đặt ở đây (không phải trong FraudLogService) để lời gọi
   * @Async đi qua proxy.
   */
  public AppException onBlocked(
    Integer userId, String ip, String fingerprint, FraudDetectedException e) {
    fraudLogService.logAsync(userId, ip, fingerprint, e.getReason());
    return new AppException(FraudErrorCode.from(e.getReason()));
  }
}
