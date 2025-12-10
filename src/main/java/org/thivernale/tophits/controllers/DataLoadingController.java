package org.thivernale.tophits.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thivernale.tophits.models.Track;
import org.thivernale.tophits.services.DataLoadingService;

import java.util.List;

@Controller
@RequestMapping("/data")
@RequiredArgsConstructor
@Slf4j
public class DataLoadingController {
    private final DataLoadingService dataLoadingService;

    @GetMapping("/load")
    public String loadDataPage(Model model) {
        model.addAttribute("csvFiles", dataLoadingService.getAvailableCsvFiles());
        return "data-load";
    }

    @GetMapping("/files")
    @ResponseBody
    public ResponseEntity<List<String>> golAvailableFiles() {
        return ResponseEntity.ok(dataLoadingService.getAvailableCsvFiles());
    }

    @PostMapping("/load/{filename}")
    @ResponseBody
    public ResponseEntity<LoadResponse> loadCsvFile(@PathVariable("filename") String fileName) {
        try {
            log.info("Loading file {}", fileName);
            var result = dataLoadingService.loadCsvFile(fileName);
            return ResponseEntity.ok(new LoadResponse(true, "", result));
        } catch (Exception e) {
            log.error("Error loading file {}", fileName, e);
            return ResponseEntity.badRequest()
                .body(new LoadResponse(false, "Error loading file: " + e.getMessage(), 0, 0, List.of(e.getMessage())));
        }
    }

    @PostMapping("/test")
    @ResponseBody
    public ResponseEntity<String> testDatabaseInsertion() {
        try {
            Track track = dataLoadingService.testDatabaseInsertion();
            log.info("Test track saved with ID: {}", track.getId());
            return ResponseEntity.ok("Test track inserted successfully with ID: " + track.getId());
        } catch (Exception e) {
            log.error("Error inserting test track", e);
            return ResponseEntity.badRequest()
                .body("Error inserting test track: " + e.getMessage());
        }
    }

    public record LoadResponse(boolean success, String message, int successCount, int errorCount, List<String> errors) {
        public LoadResponse(boolean success, String message, DataLoadingService.LoadResult result) {
            this(success, message, result.successCount(), result.errorCount(), result.errors());
        }
    }
}
