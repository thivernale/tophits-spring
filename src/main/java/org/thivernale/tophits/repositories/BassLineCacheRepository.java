package org.thivernale.tophits.repositories;

import org.springframework.data.repository.CrudRepository;
import org.thivernale.tophits.models.BassLineCache;

import java.util.Optional;

public interface BassLineCacheRepository extends CrudRepository<BassLineCache, Long> {
    Optional<BassLineCache> findByArtistNameAndTrackName(String artistName, String trackName);
}
