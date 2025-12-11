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
import org.thivernale.tophits.services.BassLineService;
import org.thivernale.tophits.services.TrackService;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/tracks")
@RequiredArgsConstructor
@Slf4j
public class TrackController {
    private final TrackService trackService;
    private final BassLineService bassLineService;

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
    public ResponseEntity<Track> getTrackById(@PathVariable Long id) {
        Track track = trackService.findById(id);
        if (track != null) {
            return ResponseEntity.ok(track);
        } else {
            return ResponseEntity.notFound()
                .build();
        }
    }

    @PostMapping("/api/{id}/bass-line")
    @ResponseBody
    public ResponseEntity<BassLineResponse> generateBassLine(@PathVariable Long id) {
        log.info("Generating Bass Line for track: {}", id);

        try {
            Track track = trackService.findById(id);
            if (track != null) {
                return ResponseEntity.ok(new BassLineResponse(id, track.getTrackName(), track.getArtistName(), bassLineService.generateBassLine(track), null));
            } else {
                return ResponseEntity.notFound()
                    .build();
            }
        } catch (Exception e) {
            log.error("Error generating Bass Line: {}", e.getMessage());

            return ResponseEntity.internalServerError()
                .body(new BassLineResponse(id, null, null, "There was an error generating bass line tabs. Please, try again later.", e.getMessage()));
        }
    }

    // TODO extract midi notes

    public record BassLineResponse(Long id, String trackName, String artistName, String content, String error) {
    }

    public record TracksResponse(
        List<Track> tracks,
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
