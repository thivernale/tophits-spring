package org.thivernale.tophits.redis;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveGeoOperations;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.thivernale.tophits.repositories.TrackRepository;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Configuration
@EnableCaching
@Slf4j
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
                .doOnNext(log::debug)
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
                .doOnNext(log::debug)
                .thenMany(listTemplate.leftPop(listName))
                .doOnNext(log::debug)
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

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic("pubsub-topic");
    }

    @Bean
    MessageListenerAdapter messageListenerAdapter() {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter();
        Jackson2JsonRedisSerializer<Response> serializer = new Jackson2JsonRedisSerializer<>(Response.class);
        messageListenerAdapter.setDelegate(new MessageListener() {
            @Override
            public void onMessage(@NonNull Message message, byte[] pattern) {
                log.warn("Received message: {}", message);

                try {
                    Response response = serializer.deserialize(message.getBody());
                    log.info("Deserialized response: {}", response);
                } catch (Exception e) {
                    log.error("Failed to deserialize message", e);
                }
            }
        });
        messageListenerAdapter.setSerializer(
            serializer
        );
        return messageListenerAdapter;
    }

    @Bean
    RedisMessageListenerContainer messageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(messageListenerAdapter(), topic());
        return redisMessageListenerContainer;
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setDefaultSerializer(
            new Jackson2JsonRedisSerializer<>(Response.class)
        );
        return template;
    }

    private Response time(ExpensiveService service, StopWatch stopWatch, double input) {
        stopWatch.start();
        Response response = service.performExpensiveCalculation(input);
        stopWatch.stop();
        log.debug("Response {} after {} seconds", response, stopWatch.lastTaskInfo()
            .getTimeMillis() / 1000.0);
        return response;
    }

    public record Response(String message) implements Serializable {
    }

    @Service
    @RequiredArgsConstructor
    public static class ExpensiveService {

        private final RedisTemplate<Object, Object> redisTemplate;

        private final ChannelTopic topic;

        private final TrackRepository trackRepository;

        @Cacheable(key = "#input", cacheNames = "expensiveCalculation")
        @SneakyThrows
        public Response performExpensiveCalculation(double input) {
            Thread.sleep(5_000);
            return new Response("Response from %f at %s%n%n".formatted(input, Instant.now()));
        }

        public Response nonCachedCalculation(double input) {
            Response result = new Response("Response from %f at %s%n%n".formatted(input, Instant.now()));

            redisTemplate.convertAndSend(topic.getTopic(), result);

            return result;
        }

        public void saveTrack(long trackId) {
            trackRepository.findById(trackId)
                .ifPresent(track -> {
                    System.out.println("Track found: " + track.getTrackName());
//                redisTemplate.opsForHash()
//                    .put("tracks-hash", trackId, track);
                    Object o = redisTemplate.opsForHash()
                        .get("tracks-hash", trackId);
                    log.info("From Redis: {}", o);
                });
        }

    }
}
