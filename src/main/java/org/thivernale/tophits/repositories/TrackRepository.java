package org.thivernale.tophits.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.thivernale.tophits.models.Track;

public interface TrackRepository extends CrudRepository<Track, Long>, PagingAndSortingRepository<Track, Long> {
}
