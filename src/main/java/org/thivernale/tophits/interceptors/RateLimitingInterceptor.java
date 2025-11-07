package org.thivernale.tophits.interceptors;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.thivernale.tophits.filters.RateLimiter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Priority(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitingInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter;

    public RateLimitingInterceptor(/*@Qualifier(value = "redisRateLimiter")*/ RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (rateLimiter.isRateLimitExceeded(request)) {
            response.setStatus(HttpServletResponseLocal.SC_TOO_MANY_REQUESTS);
            response.getWriter()
                .write("Interceptor: You have reached the maximum rate limit.");
            return false;
        }
        return true;
    }

    interface HttpServletResponseLocal {
        int SC_TOO_MANY_REQUESTS = 429;
    }
}
