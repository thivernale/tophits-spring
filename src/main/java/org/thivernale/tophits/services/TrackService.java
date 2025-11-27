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

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackService {
    private final TrackRepository trackRepository;

    public Track findById(Long id) {
        return trackRepository.findById(id)
            .orElse(null);
    }

    public long getTotalTrackCount() {
        return trackRepository.count();
    }

    public Page<Track> listTracks(int page, int size, String sortField, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc") ?
            Sort.by(sortField)
                .descending() :
            Sort.by(sortField)
                .ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return trackRepository.findAll(pageable);
    }

    public Page<Track> searchTracks(String query, String searchType, int page, int size, String sortField, String sortOrder) {
        // initialize input parameters with default values if necessary
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (sortField == null || sortField.isEmpty()) sortField = "id";
        if (sortOrder == null || (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")))
            sortOrder = "asc";
        if (searchType == null || (!searchType.equalsIgnoreCase("artist") && !searchType.equalsIgnoreCase("track")))
            searchType = "track";

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
