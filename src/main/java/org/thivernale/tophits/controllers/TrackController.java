package org.thivernale.tophits.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thivernale.tophits.services.TrackService;

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
    // endpoint for fetching single track details by ID as JSON
    // TODO search tracks with pagination and sorting, bass line and midi file
}
