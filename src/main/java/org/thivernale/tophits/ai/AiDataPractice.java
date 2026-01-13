package org.thivernale.tophits.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import redis.clients.jedis.JedisPooled;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@Profile("ai-data-practice")
public class AiDataPractice {
    @Bean
    VectorStore vectorStore(
        JedisPooled jedisPooled,
        @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel
    ) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
//            .indexName("custom-index")     // Optional: defaults to "spring-ai-index"
//            .prefix("custom-prefix")       // Optional: defaults to "embedding:"
            .vectorAlgorithm(RedisVectorStore.Algorithm.HSNW)
            .initializeSchema(true)
            .build();
    }

    @Bean
    ApplicationRunner initVectorStore(
        VectorStore vectorStore,
        @Qualifier("chatClientPractice") ChatClient ai
    ) {
        return _ -> {
            setup(vectorStore);

            PromptTemplate template = PromptTemplate.builder()
                .template("What is the text with id {id}?")
                .build();
            Map<String, Object> params = Map.of("id", "1");
            ai.prompt(template.create(params));
        };
    }

    /**
     * Create a ChatClient with a VectorStore-based QuestionAnswerAdvisor
     * when only one ChatModel is available in the classpath
     */
    @Bean
    ChatClient chatClientPractice(
        ChatClient.Builder builder,
        @Value("classpath:/system.md") Resource system,
        VectorStore vectorStore
    ) {
        return builder.defaultSystem(system)
            .defaultAdvisors(
                QuestionAnswerAdvisor.builder(vectorStore)
                    .build()
            )
            .build();
    }

    /**
     * Create a ChatClient with a OpenAiChatModel and a VectorStore-based QuestionAnswerAdvisor
     * when multiple ChatModels are available in the classpath
     */
    // @Bean
    ChatClient openAiChatClient(OpenAiChatModel chatModel, VectorStore vectorStore) {
        return ChatClient.create(chatModel)
            .mutate()
            .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(
                    SearchRequest.builder()
                        .similarityThreshold(0.8f)
                        .filterExpression(
                            "category == 'knowledge'"
                        )
                        .build()
                )
                .build()
            )
            .build();
    }

    private void setup(VectorStore vectorStore) {
        // clear all existing vectors in redis vector store
//        jedisPooled.ftDropIndexDD("custom-index");

        // check if Redis vector store contains index
        boolean indexExists = vectorStore.<JedisPooled>getNativeClient()
            .map(client -> client.ftList()
                .contains("spring-ai-index"))
            .orElse(false);

        if (!indexExists) {
            log.debug("Redis vector store is empty, initializing with sample data...");
            ingestSampleData(vectorStore);
        } else {
            log.debug("Redis vector store already initialized.");
        }

        List<Document> documents1 = vectorStore.similaritySearch(SearchRequest.builder()
            .query("text1")
            .similarityThreshold(0.9f)
            .build());

        log.info("Found {} documents:\n{}",
            documents1.size(),
            documents1.stream()
                .map(d -> d.getText() + " (" + d.getScore() + ")")
                .collect(Collectors.joining(",\n")));
    }

    private void ingestSampleData(VectorStore vectorStore) {
        List<Document> documents = List.of(Document.builder()
            .id("1")
            .text("text1")
            .metadata("category", "knowledge")
            .build(), Document.builder()
            .id("2")
            .text("something else")
            .metadata("category", "information")
            .build());

        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> split = splitter.split(documents);

        vectorStore.accept(split);
    }
}
