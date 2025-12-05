package org.thivernale.tophits.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thivernale.tophits.redis.RedisDataPractice;

@RestController
@RequestMapping("/api/rate-limited")
@RequiredArgsConstructor
public class RateLimitedController {
    private final RedisDataPractice.ExpensiveService expensiveService;

    @GetMapping
    public ResponseEntity<String> rateLimited() {
        return ResponseEntity.ok("Access granted to rate-limited endpoint.");
    }

    @GetMapping("/expensive")
    public ResponseEntity<RedisDataPractice.Response> expensiveOperation(
        @RequestParam(defaultValue = "42") double input) {
        return ResponseEntity.ok(expensiveService.performExpensiveCalculation(input));
    }

    @GetMapping("/non-cached")
    public ResponseEntity<RedisDataPractice.Response> nonCachedOperation(
        @RequestParam(defaultValue = "42") double input) {
        return ResponseEntity.ok(expensiveService.nonCachedCalculation(input));
    }
}
