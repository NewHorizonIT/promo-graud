package group2d.promo_graud.modules.order;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import group2d.promo_graud.modules.orders.service.DistributedLockService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Distributed Lock Service Unit Test")
class DistributedLockServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;

    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private DistributedLockService distributedLockService;

    private final String lockKey = "lock:voucher:1";
    private final String lockValue = "test-lock-uuid";
    private final long lockTime = 10L;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("TC1: Lấy lock thành công ngay lần đầu")
    void tryLock_firstAttemptSuccess_returnsTrue() {
        when(valueOperations.setIfAbsent(lockKey, lockValue, Duration.ofSeconds(lockTime)))
                .thenReturn(true);
        boolean result = distributedLockService.tryLock(lockKey, lockValue, lockTime);
        assertTrue(result);
    }

    @Test
    @DisplayName("TC2: Lần đầu thất bại, lần thứ hai lấy lock thành công")
    void tryLock_secondAttemptSuccess_returnsTrue() {
        when(valueOperations.setIfAbsent(lockKey, lockValue, Duration.ofSeconds(lockTime)))
                .thenReturn(false, true);
        boolean result = distributedLockService.tryLock(lockKey, lockValue, lockTime);
        assertTrue(result);
    }

    @Test
    @DisplayName("TC3: Hết thời gian chờ nhưng không lấy được lock")
    void tryLock_timeout_returnsFalse() {
        when(valueOperations.setIfAbsent(lockKey, lockValue, Duration.ofSeconds(lockTime)))
                .thenReturn(false);
        boolean result = distributedLockService.tryLock(lockKey, lockValue, lockTime);
        assertFalse(result);
    }

    @Test
    @DisplayName("TC4: Thread bị interrupt khi đang chờ lấy lock")
    void tryLock_interrupted_returnsFalse() {
        when(valueOperations.setIfAbsent(lockKey, lockValue, Duration.ofSeconds(lockTime)))
                .thenReturn(false);

        try {
            Thread.currentThread().interrupt();

            boolean result = distributedLockService.tryLock(lockKey, lockValue, lockTime);

            assertFalse(result);
            assertTrue(Thread.currentThread().isInterrupted());

        } finally {
            Thread.interrupted();
        }
    }
}
