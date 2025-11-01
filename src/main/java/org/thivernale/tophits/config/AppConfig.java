package org.thivernale.tophits.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thivernale.tophits.interceptors.LoggingInterceptor;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final LoggingInterceptor interceptor;

    public AppConfig(LoggingInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
            .addPathPatterns("/**");
    }
}
