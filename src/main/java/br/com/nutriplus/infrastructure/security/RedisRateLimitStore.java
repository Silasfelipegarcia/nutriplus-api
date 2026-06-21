package br.com.nutriplus.infrastructure.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Optional Redis-backed rate limit counters when {@code spring.data.redis.host} is configured.
 */
@Component
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisRateLimitStore {

    private final StringRedisTemplate redis;

    public RedisRateLimitStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean incrementAndCheck(String key, int limit, long windowSeconds) {
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return count != null && count > limit;
    }

    public void recordSecurityEvent(String ip, String action) {
        String key = "security:ip:" + ip + ":" + Instant.now().getEpochSecond() / 60;
        redis.opsForValue().increment(key);
        redis.expire(key, 5, TimeUnit.MINUTES);
    }
}
