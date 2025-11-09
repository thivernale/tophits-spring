package org.thivernale.tophits.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import redis.clients.jedis.JedisPooled;

import java.util.List;

@Slf4j
@Configuration
public class AiDataPractice {
    @Bean
    JedisPooled jedisPooled() {
        return new JedisPooled();
    }

    @Bean
    VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        RedisVectorStore vectorStore = RedisVectorStore.builder(jedisPooled, embeddingModel)
//            .indexName("custom-index")     // Optional: defaults to "spring-ai-index"
//            .prefix("custom-prefix")       // Optional: defaults to "embedding:"
            .vectorAlgorithm(RedisVectorStore.Algorithm.HSNW)
            .initializeSchema(true)
            .build();
        return vectorStore;
    }

    @Bean
    ApplicationRunner initVectorStore(VectorStore vectorStore, JedisPooled jedisPooled, ChatClient ai) {
        return args -> {
            setup(vectorStore, jedisPooled);

//        ai.prompt("");
        };
    }

    @Bean
    ChatClient chatClient(
        ChatClient.Builder builder,
        @Value("classpath:/system.md") Resource system,
        VectorStore vectorStore) {
        return builder.defaultSystem(system)
            .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                .build())
            .build();
    }

    private void setup(VectorStore vectorStore, JedisPooled jedisPooled) {
        // clear all existing vectors in redis vector store
//        jedisPooled.ftDropIndexDD("custom-index");

        TokenTextSplitter splitter = new TokenTextSplitter();

        List<Document> documents = List.of(Document.builder()
            .id("1")
            .text("text1")
            .metadata("category", "knowledge")
            .build(), Document.builder()
            .id("2")
            .text("text2")
            .metadata("category", "information")
            .build());
        List<Document> split = splitter.split(documents);
        vectorStore.accept(documents);

        List<Document> documents1 = vectorStore.similaritySearch(SearchRequest.builder()
            .query("text1")
            .build());
        log.info("Found {} documents", documents1.size());
    }

}
