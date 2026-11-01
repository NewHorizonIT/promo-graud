package group2d.promo_graud.modules.orders.service;

import java.time.Duration;
import java.util.Collections;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistributedLockService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final DefaultRedisScript<Long> REDIS_SCRIPT;

    static {
        REDIS_SCRIPT = new DefaultRedisScript<>();
        REDIS_SCRIPT.setScriptText(
                """
                if redis.call('GET', KEYS[1]) == ARGV[1] then
                    return redis.call('DEL', KEYS[1])
                else
                    return 0
                end
                """);
        REDIS_SCRIPT.setResultType(Long.class);
    }

    public boolean tryLock(String lockKey, String lockValue, long lockTime) {
        long timeout = 1000;
        long retryInterval = 10;
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
        redisTemplate.execute(REDIS_SCRIPT, Collections.singletonList(lockKey), lockValue);
    }
}
