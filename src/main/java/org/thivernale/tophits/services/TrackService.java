package org.thivernale.tophits.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.thivernale.tophits.models.Track;
import org.thivernale.tophits.repositories.TrackRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackService {
    private final TrackRepository trackRepository;

    public Track findById(Long id) {
        return trackRepository.findById(id)
            .orElse(null);
    }

    public List<Track> findAll() {
        return new ArrayList<>((Collection) trackRepository.findAll());
    }

    public Page<Track> searchTracks(String query, String searchType, int page, int size, String sortField, String sortOrder) {
        log.info("Searching tracks with query: {}, searchType: {}, page: {}, size: {}, sortField: {}, sortOrder: {}",
            query, searchType, page, size, sortField, sortOrder);

        Sort sort = sortOrder.equalsIgnoreCase("desc") ? Sort.by(sortField)
            .descending() : Sort.by(sortField)
            .ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (searchType.equalsIgnoreCase("artist"))
            return trackRepository.findByArtistNameContainingIgnoreCase(query, pageable);
        else {
            return trackRepository.findByTrackNameContainingIgnoreCase(query, pageable);
        }
    }
}
