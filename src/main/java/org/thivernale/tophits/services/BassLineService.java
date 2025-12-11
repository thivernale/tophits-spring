package org.thivernale.tophits.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thivernale.tophits.models.BassLineCache;
import org.thivernale.tophits.models.Track;
import org.thivernale.tophits.repositories.BassLineCacheRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BassLineService {
    private final BassLineCacheRepository bassLineCacheRepository;

    public String generateBassLine(Track track) {
        log.info("Generating Bass Line for Track {}", track);
        Optional<BassLineCache> bassLineCache = bassLineCacheRepository.findByArtistNameAndTrackName(track.getArtistName(), track.getTrackName());
        if (bassLineCache.isPresent()) {
            return bassLineCache.get()
                .getBassLineContent();
        } else {
            // TODO
            throw new RuntimeException("Bass Line Cache Not Found");
        }
    }
}
