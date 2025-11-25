package org.thivernale.tophits.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thivernale.tophits.services.TrackService;

@Controller
@RequestMapping("/tracks")
@RequiredArgsConstructor
@Slf4j
public class TrackController {
    private final TrackService trackService;
}
