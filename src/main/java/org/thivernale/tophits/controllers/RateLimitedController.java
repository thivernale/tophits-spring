package org.thivernale.tophits.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rate-limited")
public class RateLimitedController {
    @GetMapping
    public ResponseEntity<String> rateLimited() {
        return ResponseEntity.ok("Access granted to rate-limited endpoint.");
    }
}
