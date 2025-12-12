package org.thivernale.tophits.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.thivernale.tophits.models.BassLineCache;
import org.thivernale.tophits.models.Track;
import org.thivernale.tophits.repositories.BassLineCacheRepository;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BassLineService {
    private final BassLineCacheRepository bassLineCacheRepository;
    private final ChatClient chatClient;

    public String generateBassLine(Track track) {
        log.info("Generating Bass Line for Track {} by {}", track.getTrackName(), track.getArtistName());

        Optional<BassLineCache> bassLineCache = bassLineCacheRepository.findByArtistNameAndTrackName(track.getArtistName(), track.getTrackName());
        if (bassLineCache.isPresent()) {
            log.info("Found Bass Line for Track {} by {}", track.getTrackName(), track.getArtistName());
            return bassLineCache.get()
                .getBassLineContent();
        }

        log.info("No cached result found, calling OpenAI API for Track {} by {}", track.getTrackName(), track.getArtistName());
        String prompt = buildPrompt(track);

        final String content = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        try {
            if (content != null) {
                try {
                    BassLineCache newCache = BassLineCache.builder()
                        .artistName(track.getArtistName())
                        .trackName(track.getTrackName())
                        .bassLineContent(content)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                    bassLineCacheRepository.save(newCache);
                    log.info("Saved Bass Line Cache for Track {} by {}", track.getTrackName(), track.getArtistName());
                } catch (Exception e) {
                    log.error("Failed to save Bass Line Cache for Track {} by {}", track.getTrackName(), track.getArtistName());
                }
            }

            log.info("Successfully generated Bass Line tabs for Track {}", track.getTrackName());
            return content;
        } catch (Exception e) {
            log.error("Error while generating Bass Line tabs for Track {} by {}: {}", track.getTrackName(), track.getArtistName(), e.getMessage());
            return "Error while generating Bass Line tabs. Please try again later.";
        }
    }

    private String buildPrompt(Track track) {
        StringBuilder builder = new StringBuilder().append(
                "Generate bass line tabs for the song %s by %s."
                    .formatted(track.getTrackName(), track.getArtistName()))
            .append(" Released in %d".formatted(track.getReleasedYear()));
        builder.append("""
            
             Please provide:
             1. A simple bass line pattern in tablature format (4-string bass)")
             2. Chord progression suggestions
             3. Playing tips and techniques
             4. Rhythm pattern recommendations
            
             Format the response in a clear, readable way with proper sections.
            """);
        return builder.toString();
    }
}
