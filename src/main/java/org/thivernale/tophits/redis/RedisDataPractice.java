package org.thivernale.tophits.redis;

import lombok.SneakyThrows;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.ReactiveGeoOperations;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisDataPractice {
    @Bean
    @ConditionalOnProperty("app.redis.data.enabled")
    ApplicationRunner geography(ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        return args -> {
            ReactiveGeoOperations<String, String> geoTemplate = reactiveRedisTemplate.opsForGeo();
            String localRegion = "Local Region";

            Flux.fromIterable(
                    Map.of("Vn", new Point(27.9150592, 43.204608), "Bchk", new Point(28.1308318, 43.4160519))
                        .entrySet()
                )
                .flatMap(entry -> geoTemplate.add(localRegion, entry.getValue(), entry.getKey()))
                .thenMany(geoTemplate.radius(
                    localRegion,
                    new Circle(
                        new Point(28.1308318, 43.4160519),
                        new Distance(50, Metrics.KILOMETERS)
                    )))
                .map(GeoResult::getContent)
                .map(GeoLocation::getName)
                .doOnNext(System.out::println)
                .subscribe();
        };
    }

    @Bean
    @ConditionalOnProperty("app.redis.data.enabled")
    ApplicationRunner list(ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        return args -> {
            ReactiveListOperations<String, String> listTemplate = reactiveRedisTemplate.opsForList();
            var listName = "assorted-item-list";
            var push = listTemplate.leftPushAll(listName, "value1", "value2", "value3");
            push.thenMany(listTemplate.leftPop(listName))
                .doOnNext(System.out::println)
                .thenMany(listTemplate.leftPop(listName))
                .doOnNext(System.out::println)
                .subscribe();
        };
    }

    @Bean
    ApplicationRunner expensiveResponse(ExpensiveService service) {
        return args -> {
            final double input = 42;
            StopWatch stopWatch = new StopWatch();
            time(service, stopWatch, input);
            time(service, stopWatch, input);
        };
    }

    private Response time(ExpensiveService service, StopWatch stopWatch, double input) {
        stopWatch.start();
        Response response = service.performExpensiveCalculation(input);
        stopWatch.stop();
        System.out.printf("Response %s after %f seconds %n%n", response, stopWatch.lastTaskInfo()
            .getTimeMillis() / 1000.0);
        return response;
    }

    record Response(String message) implements Serializable {
    }

    @Service
    static class ExpensiveService {
        @Cacheable(value = "expensiveCalculation")
        @SneakyThrows
        public Response performExpensiveCalculation(double input) {
            Thread.sleep(5_000);
            return new Response("Response from %f at %s%n%n".formatted(input, Instant.now()));
        }
    }
}
