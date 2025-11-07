package org.thivernale.tophits.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisRateLimiter extends RateLimiter {
    private final RedisTemplate<String, String> redisTemplate;

    private final Duration timeout = Duration.ofSeconds(10);
    @Value("${ratelimiter.limit:3}")
    private int limit; // max requests

    public RedisRateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean isAllowed(String userId) {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

        // init or increment counter
        Long userRequestCount = opsForValue.increment(userId, 1L);
        // init expiration (subsequent increments will not reset it)
        if (userRequestCount != null && userRequestCount == 1L) {
            redisTemplate.expire(userId, timeout.getSeconds(), TimeUnit.SECONDS);
        }

        log.info("userRequestCount={}", userRequestCount);
        log.info("expire={}", opsForValue.getOperations()
            .getExpire(userId));

        return userRequestCount != null && userRequestCount <= limit;
    }
}
