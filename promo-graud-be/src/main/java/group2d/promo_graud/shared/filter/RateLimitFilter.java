package group2d.promo_graud.shared.filter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.shared.dto.ApiResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${rate-limit.requests-per-window:5}")
    private int requestsPerWindow;

    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;

    private static final String ORDER_PATH = "/api/v1/orders";
    private static final String POST_METHOD = "POST";

    // Atomic Lua script: xóa entries hết hạn → đếm → nếu đủ chỗ thì ZADD → trả 1; nếu full trả 0
    // Dùng Lua để đảm bảo cả 4 bước chạy atomically, không race condition
    private static final DefaultRedisScript<Long> SLIDING_WINDOW_SCRIPT;

    static {
        SLIDING_WINDOW_SCRIPT = new DefaultRedisScript<>();
        SLIDING_WINDOW_SCRIPT.setScriptText(
                """
                local now    = tonumber(ARGV[1])
                local win    = tonumber(ARGV[2])
                local limit  = tonumber(ARGV[3])
                local member = ARGV[4]
                redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, now - win * 1000)
                local count = tonumber(redis.call('ZCARD', KEYS[1]))
                if count >= limit then return 0 end
                redis.call('ZADD', KEYS[1], now, member)
                redis.call('EXPIRE', KEYS[1], win)
                return 1
                """);
        SLIDING_WINDOW_SCRIPT.setResultType(Long.class);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!isTargetEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = extractIp(request);
        String key = "rl:order:" + ip;
        long now = System.currentTimeMillis();
        // member = now + UUID để tránh duplicate member khi 2 request đến cùng ms
        String member = now + "_" + UUID.randomUUID();

        Long allowed;
        try {
            allowed =
                    redisTemplate.execute(
                            SLIDING_WINDOW_SCRIPT,
                            List.of(key),
                            String.valueOf(now),
                            String.valueOf(windowSeconds),
                            String.valueOf(requestsPerWindow),
                            member);
        } catch (Exception e) {
            // Fail-open: Redis down không được làm sập order flow
            log.warn("Rate limit Redis unavailable, failing open: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (allowed == null) {
            log.warn("Rate limit script returned null for key={}, treating as allowed", key);
            filterChain.doFilter(request, response);
            return;
        }

        if (allowed == 0L) {
            log.warn("Rate limit hit: ip={}, key={}", ip, key);
            writeRateLimitResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTargetEndpoint(HttpServletRequest request) {
        return POST_METHOD.equalsIgnoreCase(request.getMethod())
                && ORDER_PATH.equals(request.getServletPath());
    }

    // Copy y chang OrderController.extractIp() để đảm bảo cùng logic IP, tránh bypass qua header
    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) {
            return request.getRemoteAddr();
        }
        String[] parts = forwarded.split(",");
        return parts[parts.length - 1].trim();
    }

    // GlobalExceptionHandler không bắt được exception từ filter — phải viết JSON thủ công
    private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
        RateLimitErrorCode error = RateLimitErrorCode.RATE_LIMIT_EXCEEDED;
        response.setStatus(error.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<?> body =
                ApiResponse.builder().code(error.getCode()).message(error.getMessage()).build();
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(body));
        response.flushBuffer();
    }
}
