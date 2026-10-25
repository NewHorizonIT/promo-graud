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

    // Lấy khóa phân tán trên Redis, nếu đang bị khóa thì retry, quá 4 giây thì timeout
    // setIfAbsent kèm TTL -> thành công trả true -> thất bại thì sleep 10ms rồi thử lại -> hết 4
    // giây trả false
    public boolean tryLock(String lockKey, String lockValue, long lockTime) {
        long timeout = 4000;
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

    // Nhả khóa an toàn, chỉ xóa khi khóa đúng là của request hiện tại (tránh request A xóa nhầm
    // khóa của request B)
    // Chạy Lua script atomic trên Redis: GET key -> so sánh với lockValue -> trùng thì DEL, khác
    // thì bỏ qua
    public void unLock(String lockKey, String lockValue) {
        String script =
                """
                if redis.call('GET', KEYS[1]) == ARGV[1] then
                    return redis.call('DEL', KEYS[1])
                else
                    return 0
                end
                """;
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);
        redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
    }
}
