package group2d.promo_graud.modules.fraud;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudLogService {

  private final FraudLogRepository repository;

  @Async("fraudLogExecutor")
  public void logAsync(Integer userId, String ip, String fingerprint, FraudReason reason) {
    try {
      repository.save(
        FraudLog.builder()
          .userId(userId)
          .ipAddress(ip)
          .deviceFingerprint(fingerprint)
          .reason(reason)
          .build());
    } catch (Exception e) {
      log.warn("Cannot write fraud_log (user={}, reason={})", userId, reason, e);
    }
  }
}
