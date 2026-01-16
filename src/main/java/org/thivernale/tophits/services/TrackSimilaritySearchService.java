package org.thivernale.tophits.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.thivernale.tophits.models.Track;
import redis.clients.jedis.JedisPooled;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackSimilaritySearchService {
    private final VectorStore vectorStore;
    private final JdbcClient db;
    private final TokenTextSplitter splitter = new TokenTextSplitter(true);

    @Value("${spring.ai.vectorstore.redis.index-name}")
    private String indexName;
    @Value("${spring.ai.vectorstore.redis.prefix}")
    private String prefix;

    public void setup() {
        boolean indexExists = vectorStore.<JedisPooled>getNativeClient()
            .map(client -> client.ftList()
                .contains(indexName) && !client.keys(prefix + "*")
                .isEmpty())
            .orElse(false);

        if (!indexExists) {
            log.warn("Redis vector store is empty, initializing with sample data...");
            ingestTracks();
            log.debug("Redis vector store initialized with sample data.");
        } else {
            log.warn("Redis vector store already initialized.");
        }
    }

    @SuppressWarnings("unused")
    private void clearVectorStore() {
        vectorStore.<JedisPooled>getNativeClient()
            .ifPresent(client -> client.ftDropIndexDD(indexName));
        log.info("Cleared Redis vector store index: {}", indexName);
    }

    void ingestTracks() {
        var tracks = db.sql("SELECT * FROM tracks WHERE id < 1000")
            .query(new DataClassRowMapper<>(Track.class))
            .list();

        TrackVectorDocGenerator docGenerator = new TrackVectorDocGenerator();

        tracks.parallelStream()
            .map(getTrackDocumentFunction(docGenerator))
            .map(splitter::split)
            // vectorStore::accept shorthand
            .forEach(vectorStore);
    }

    private Function<Track, Document> getTrackDocumentFunction(TrackVectorDocGenerator docGenerator) {
        return track -> Document.builder()
            .id(String.valueOf(track.getId()))
            .text(docGenerator.generateSemanticRepresentation(track))
            .metadata(docGenerator.generateMetadata(track))
            .build();
    }

    public String similaritySearchTrack(String query) {
        var results = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(5)
                .build()
        );

        return "Similar results to: '%s'.%n%n".formatted(query) + results.stream()
            .map(document -> document.getText() + " (Score: " + evaluateSimilarityScore(document.getScore()) + ")")
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private String evaluateSimilarityScore(Double score) {
        switch (score) {
            case null -> {
                return "No Score";
            }
            case double s when s >= 0.85 -> {
                return "Very High";
            }
            case double s when s >= 0.7 -> {
                return "High (Fuzzy Match)";
            }
            default -> {
                return "Low (Noise)";
            }
        }
    }
}
