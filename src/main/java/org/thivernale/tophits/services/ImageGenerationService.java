package org.thivernale.tophits.services;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.stereotype.Service;
import org.thivernale.tophits.models.Track;

@Service
@RequiredArgsConstructor
public class ImageGenerationService {
    private final ImageModel imageModel;

    public String generateImageForTrack(Track track) {
        String prompt = "Create a vibrant and eye-catching album cover for a music track titled '"
            + track.getTrackName() + "' by the artist '" + track.getArtistName() + "'. "
            + "The design should resemble the original cover art. "
            + "Ensure the text is legible and complements the overall design.";
        return generateImage(prompt);
    }

    public String generateImage(String prompt) {
        return imageModel.call(new ImagePrompt(prompt))
            .getResult()
            .getOutput()
            .getUrl();
    }
}
