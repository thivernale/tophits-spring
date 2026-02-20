package org.thivernale.tophits.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.thivernale.tophits.services.TrackSimilaritySearchService;
import redis.clients.jedis.JedisPooled;

@Configuration
class VectorStoreConfig {
    @Bean
    JedisPooled jedisPooled() {
        return new JedisPooled();
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(RedisStandaloneConfiguration redisStandaloneConfiguration) {
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration(
        @Value("${spring.data.redis.host:localhost}") String host,
        @Value("${spring.data.redis.port:6379}") int port
    ) {
        return new RedisStandaloneConfiguration(host, port);
    }

    @Bean
    ApplicationRunner initVectorStore(
        TrackSimilaritySearchService trackSimilaritySearchService
    ) {
        return _ -> {
//            trackSimilaritySearchService.clearVectorStore();
            trackSimilaritySearchService.setup();
        };
    }
}
