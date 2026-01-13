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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackSimilaritySearchService {
    private final VectorStore vectorStore;
    private final JedisPooled jedisPooled;
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

    void ingestTracks() {
        var tracks = db.sql("SELECT * FROM tracks WHERE id < 100")
            .query(new DataClassRowMapper<>(Track.class))
            .list();

        tracks.parallelStream()
            .map(track -> Document.builder()
                .id(String.valueOf(track.getId()))
                .text(
                    "'%s' by %s, released in %d with %d streams.".formatted(
                        track.getTrackName(), track.getArtistName(), track.getReleasedYear(), track.getStreams())
                )
                .metadata(Map.of(
                    "trackName", track.getTrackName(),
                    "artistName", track.getArtistName(),
                    "releasedYear", track.getReleasedYear(),
                    "streams", track.getStreams()
                ))
                .build())
            .map(splitter::split)
            // vectorStore::accept shorthand
            .forEach(vectorStore);
    }

    public String similaritySearchTrack(Long trackId) {
        Optional<Track> trackOptional = db.sql("SELECT * FROM tracks WHERE id = :id")
            .param("id", trackId)
            .query(new DataClassRowMapper<>(Track.class))
            .optional();
        if (trackOptional.isEmpty()) {
            log.warn("Track with id {} not found in database.", trackId);
            return "";
        }

        Track track = trackOptional.get();

        var results = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query("'%s' by %s, released in %d with %d streams."
                    .formatted(
                        track
                            .getTrackName(),
                        track
                            .getArtistName(),
                        track
                            .getReleasedYear(),
                        track
                            .getStreams()
                    ))
                .topK(5)
                .build()
        );

        return "Similar results to: '%s' by %s, released in %d with %d streams.%n%n".formatted(
            track.getTrackName(), track.getArtistName(), track.getReleasedYear(), track.getStreams()) + results.stream()
            .map(Document::getText)
            .collect(Collectors.joining(System.lineSeparator()));
    }
}
