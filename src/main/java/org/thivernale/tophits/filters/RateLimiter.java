package org.thivernale.tophits.filters;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Primary
public class RateLimiter {
    private final Map<String, UserRequests> userRequests = new ConcurrentHashMap<>();
    private final long duration = TimeUnit.MINUTES.toMillis(1); // duration in milliseconds
    @Value("${ratelimiter.limit:3}")
    private int limit; // max requests

    protected boolean isAllowed(String userId) {
        long currentTime = System.currentTimeMillis();

        userRequests.compute(userId, (key, value) -> {
            if (value == null || currentTime - value.timestamp > duration) {
                // Reset if duration expired
                return new UserRequests(1, currentTime);
            }
            if (value.count <= limit) {
                value.count++;
                return value;
            }
            return value; // limit reached
        });

        return userRequests.get(userId) != null && userRequests.get(userId).count <= limit;
    }

    public boolean isRateLimitExceeded(HttpServletRequest request) {
        String userId = request.getRemoteAddr(); // or extract from headers/session
        return !isAllowed(userId);
    }

    protected static class UserRequests {
        int count;
        long timestamp;

        UserRequests(int count, long timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }
    }
}
