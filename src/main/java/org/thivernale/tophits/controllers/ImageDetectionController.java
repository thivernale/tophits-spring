package org.thivernale.tophits.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thivernale.tophits.services.ImageDetectionService;

@RestController
@RequestMapping("/api/detect-image")
@RequiredArgsConstructor
class ImageDetectionController {
    private final ImageDetectionService imageDetectionService;

    @Value("file:./data/img.png")
    private Resource sampleImage;

    @GetMapping
    public String detectImage() {
        return imageDetectionService.imageToText(sampleImage);
    }
}
