package group2d.promo_graud.modules.fraud;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudLogService {

    private final FraudLogRepository repository;

    @Async("fraudLogExecutor")
    public void logAsync(
            Integer userId,
            String ip,
            String fingerprint,
            FraudReason reason,
            java.time.LocalDateTime blockedAt) {
        try {
            repository.save(
                    FraudLog.builder()
                            .userId(userId)
                            .ipAddress(ip)
                            .deviceFingerprint(fingerprint)
                            .reason(reason)
                            .blockedAt(blockedAt)
                            .build());
        } catch (Exception e) {
            log.warn("Cannot write fraud_log (user={}, reason={})", userId, reason, e);
        }
    }
}
