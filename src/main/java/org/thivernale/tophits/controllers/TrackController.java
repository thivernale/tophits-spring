package org.thivernale.tophits.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thivernale.tophits.models.Track;
import org.thivernale.tophits.services.TrackService;

import java.util.Collections;

@Controller
@RequestMapping("/tracks")
@RequiredArgsConstructor
@Slf4j
public class TrackController {
    private final TrackService trackService;

    // endpoint for page skeleton
    @GetMapping
    public String getTracksPage(Model model) {
        log.info("serving tracks page");
        model.addAttribute("totalCount", trackService.getTotalTrackCount());
        return "tracks";
    }

    // endpoint for fetching track page by search criteria as JSON
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<TracksResponse> getTracksApi(
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "track") String searchType,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "20") Integer size,
        @RequestParam(defaultValue = "id") String sortField,
        @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        log.info("Fetching tracks with query: {}, searchType: {}, page: {}, size: {}, sortField: {}, sortOrder: {}",
            search, searchType, page, size, sortField, sortOrder);

        try {
            Page<Track> tracks = (Strings.isBlank(search)) ?
                trackService.listTracks(page, size, sortField, sortOrder) :
                trackService.searchTracks(search, searchType, page, size, sortField, sortOrder);

            TracksResponse tracksResponse = new TracksResponse(
                tracks.getContent(),
                tracks.getNumber(),
                trackService.getTotalTrackCount(),
                page,
                size,
                tracks.getTotalElements(),
                tracks.getTotalPages(),
                tracks.hasNext(),
                tracks.hasPrevious(),
                tracks.isLast(),
                tracks.isFirst(),
                null
            );

            return ResponseEntity.ok(tracksResponse);
        } catch (Exception e) {
            log.error("Error fetching tracks: {}", e.getMessage());
            TracksResponse tracksResponse = new TracksResponse(
                Collections.emptyList(),
                0,
                0,
                0,
                0,
                0,
                0,
                false,
                false,
                false,
                false,
                e.getMessage()
            );

            return ResponseEntity.badRequest()
                .body(tracksResponse);
        }
    }

    // endpoint for fetching single track details by ID as JSON
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Track> getTrackById(@PathVariable(name = "id") Long id) {
        Track track = trackService.findById(id);
        if (track != null) {
            return ResponseEntity.ok(track);
        } else {
            return ResponseEntity.notFound()
                .build();
        }
    }

    // TODO generate bass line tabs and extract midi notes

    public record TracksResponse(
        java.util.List<Track> tracks,
        int currentPage,
        long totalCount,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious,
        boolean last,
        boolean first,
        String error
    ) {
    }

    public record PageResponse<T>(
        java.util.List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
    ) {
    }

}
