package org.thivernale.tophits.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thivernale.tophits.filters.RateLimiter;
import org.thivernale.tophits.filters.RateLimitingFilter;
import org.thivernale.tophits.interceptors.LoggingInterceptor;
import org.thivernale.tophits.interceptors.RateLimitingInterceptor;
import org.thivernale.tophits.repositories.TrackRepository;
import org.thivernale.tophits.services.AppListeners;

import java.util.Arrays;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final LoggingInterceptor interceptor;
    private final RateLimitingInterceptor rateLimitingInterceptor;
    private final RateLimiter rateLimiter;

    public AppConfig(LoggingInterceptor interceptor, RateLimitingInterceptor rateLimitingInterceptor, RateLimiter rateLimiter) {
        this.interceptor = interceptor;
        this.rateLimitingInterceptor = rateLimitingInterceptor;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
            .addPathPatterns("/**");
        registry.addInterceptor(rateLimitingInterceptor)
            .addPathPatterns("/api/**")
            .order(0);
    }

    @Bean
    FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimitingFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new RateLimitingFilter(rateLimiter));
        filterRegistrationBean.addUrlPatterns("/api/v2/*");
        filterRegistrationBean.setOrder(0);
        return filterRegistrationBean;
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> publisher(ApplicationEventPublisher publisher) {
        return event -> publisher.publishEvent(new AppListeners.CustomAppEvent(event.getSource()));
    }

    //@Bean
    public CommandLineRunner demo(ApplicationContext ctx, TrackRepository repo) {
        return args -> {
            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

            System.out.println(repo.count());
        };
    }
}
