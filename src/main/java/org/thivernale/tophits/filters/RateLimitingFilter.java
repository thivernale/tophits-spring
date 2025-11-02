package org.thivernale.tophits.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

//@Component
//@FilterRegistration(name = "RateLimitingFilter", urlPatterns = {"/api/*"})
public class RateLimitingFilter implements Filter {
    private final RateLimiter rateLimiter;

    public RateLimitingFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        if (rateLimiter.isRateLimitExceeded(httpServletRequest)) {
            httpServletResponse.setStatus(HttpServletResponseLocal.SC_TOO_MANY_REQUESTS);
            httpServletResponse.getWriter()
                .write("Filter: You have reached the maximum rate limit.");
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    interface HttpServletResponseLocal {
        int SC_TOO_MANY_REQUESTS = 429;
    }
}
