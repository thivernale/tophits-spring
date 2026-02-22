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
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.thivernale.tophits.models.Track;
import org.thivernale.tophits.repositories.TrackRepository;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Slf4j
@Configuration
@EnableCaching
public class RedisCachePractice {
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // keys
        GenericToStringSerializer<Object> keySerializer = new GenericToStringSerializer<>(Object.class);
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        // values
//        Jackson2JsonRedisSerializer<Response> jsonSerializer = new Jackson2JsonRedisSerializer<>(Response.class);
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }

    public record Response(String message) implements Serializable {
    }

    @Service
    @RequiredArgsConstructor
    public static class ExpensiveService {

        private final RedisTemplate<Object, Object> redisTemplate;

        private final ChannelTopic topic;

        private final TrackRepository trackRepository;

        @Bean
        @ConditionalOnProperty("app.redis.cache.enabled")
        ApplicationRunner expensiveResponse(RedisCachePractice.ExpensiveService service) {
            return args -> {
                final double input = Double.parseDouble(LocalTime.now()
                    .format(DateTimeFormatter.ofPattern("HHmmss")));
                StopWatch stopWatch = new StopWatch();
                time(service, stopWatch, input);
                time(service, stopWatch, input);
            };
        }

        private RedisCachePractice.Response time(RedisCachePractice.ExpensiveService service, StopWatch stopWatch, double input) {
            stopWatch.start();
            RedisCachePractice.Response response = service.performExpensiveCalculation(input);
            stopWatch.stop();
            log.debug("Response {} after {} seconds", response, stopWatch.lastTaskInfo()
                .getTimeMillis() / 1000.0);
            return response;
        }

        @Cacheable(key = "#input", cacheNames = "expensive-calculation-cache")
        @SneakyThrows
        public RedisCachePractice.Response performExpensiveCalculation(double input) {
            Thread.sleep(5_000);
            return new RedisCachePractice.Response("Response from %f at %s%n%n".formatted(input, Instant.now()));
        }

        public RedisCachePractice.Response nonCachedCalculation(double input) {
            RedisCachePractice.Response result = new Response("Response from %f at %s%n%n".formatted(input, Instant.now()));

            redisTemplate.convertAndSend(topic.getTopic(), result);

            return result;
        }

        public Optional<Track> saveTrack(long trackId) {
            UnaryOperator<Track> trackFunction = track -> {
                track.setTrackName(track.getTrackName() + " (cached)");

                HashOperations<Object, Long, Track> opsForHash = redisTemplate.opsForHash();
                opsForHash.put("tracks-hash", trackId, track);

                return track;
            };
            return retrieveAndProcessTrack(trackId, trackFunction);
        }

        public Optional<Track> getTrack(long trackId) {
            UnaryOperator<Track> trackFunction = track -> {
                HashOperations<Object, Long, Track> opsForHash = redisTemplate.opsForHash();
                Track redisTrack = opsForHash.get("tracks-hash", trackId);
                RedisCachePractice.log.info("From Redis: {}", redisTrack);

                return redisTrack;
            };
            return retrieveAndProcessTrack(trackId, trackFunction);
        }

        private Optional<Track> retrieveAndProcessTrack(long trackId, UnaryOperator<Track> mapper) {
            Optional<Track> track = trackRepository.findById(trackId);

            track.ifPresent(t -> RedisCachePractice.log.info("Track found: {}", t.getTrackName()));

            return track.map(mapper);
        }
    }
}
