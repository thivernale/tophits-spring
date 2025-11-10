package org.thivernale.tophits.filters;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiterTwo {
    private final Map<String, UserRequests> userRequestsMap = new ConcurrentHashMap<>();
    private final long TIME_WINDOW_MS = TimeUnit.SECONDS.toMillis(60); // 1 minute
    private final int MAX_REQUESTS = 3; // Max requests

    public boolean isRateLimitExceeded(HttpServletRequest httpServletRequest) {
        String userId = httpServletRequest.getRemoteAddr();
        return !isUserAllowed(userId);
    }

    private boolean isUserAllowed(String userId) {
        long currentTime = System.currentTimeMillis();

        userRequestsMap.compute(userId, (key, value) -> {
            if (value == null || currentTime - value.timestamp > TIME_WINDOW_MS) {
                value = new UserRequests(1, currentTime); // reset
            } else if (value.count < MAX_REQUESTS) {
                value.count += 1;
            }

            return value;
        });

        return userRequestsMap.containsKey(userId) && userRequestsMap.get(userId).count < MAX_REQUESTS;
    }

    private static class UserRequests {
        int count;
        long timestamp;

        UserRequests(int count, long timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }
    }
}
