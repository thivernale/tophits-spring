package org.thivernale.tophits.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveGeoOperations;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.Map;

@Configuration
@Slf4j
public class RedisDataPractice {
    // needed for reactive redis template, must be created explicitly since there is jedis elsewhere
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

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

    public record Response(String message) implements Serializable {
    }
}
