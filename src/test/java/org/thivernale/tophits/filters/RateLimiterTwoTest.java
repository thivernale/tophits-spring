package org.thivernale.tophits.filters;


import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

class RateLimiterTwoTest {
    @Test
    void isRateLimitExceeded() throws NoSuchFieldException, IllegalAccessException {
        RateLimiterTwo rateLimiterTwo = new RateLimiterTwo();

        HttpServletRequest request = new MockHttpServletRequest();
        boolean exceeded = rateLimiterTwo.isRateLimitExceeded(request);

        Assertions.assertThat(exceeded)
            .isFalse();

        rateLimiterTwo.isRateLimitExceeded(request);
        rateLimiterTwo.isRateLimitExceeded(request);
        exceeded = rateLimiterTwo.isRateLimitExceeded(request);
        Assertions.assertThat(exceeded)
            .isTrue();

        Field field = RateLimiterTwo.class.getDeclaredField("TIME_WINDOW_MS");
        field.setAccessible(true);
        field.set(rateLimiterTwo, 1000);
        Object o = field
            .get(rateLimiterTwo);
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        exceeded = rateLimiterTwo.isRateLimitExceeded(request);
        Assertions.assertThat(exceeded)
            .isFalse();
    }
}
