package org.thivernale.tophits.redis;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.ReactiveGeoOperations;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoLocation;
import reactor.core.publisher.Flux;

import java.util.Map;

@Configuration
public class RedisDataPractice {
    @Bean
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
}
