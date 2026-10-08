package group2d.promo_graud.modules.orders.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistributedLockService {
    private final RedisTemplate<String, String> redisTemplate;

    public boolean tryLock(String lockKey, String lockValue, long lockTime) {
        long timeout = 50;
        long retryInterval = 5;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            Boolean isLocked =
                    redisTemplate
                            .opsForValue()
                            .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(lockTime));
            if (Boolean.TRUE.equals(isLocked)) {
                return true;
            }
            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    public void unLock(String lockKey, String lockValue) {
        String currentValue = redisTemplate.opsForValue().get(lockKey);
        if (lockValue.equals(currentValue)) {
            redisTemplate.delete(lockKey);
        }
    }
}
