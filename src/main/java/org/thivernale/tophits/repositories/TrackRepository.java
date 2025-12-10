package org.thivernale.tophits.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.thivernale.tophits.models.Track;

public interface TrackRepository extends CrudRepository<Track, Long>, PagingAndSortingRepository<Track, Long> {
    Page<Track> findByArtistNameContainingIgnoreCase(String artistName, Pageable pageable);

    Page<Track> findByTrackNameContainingIgnoreCase(String trackName, Pageable pageable);
}
