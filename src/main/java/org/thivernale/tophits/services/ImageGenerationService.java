package org.thivernale.tophits.services;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;
import org.thivernale.tophits.models.Track;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageGenerationService {
    private final ImageModel imageModel;

    public String generateImageForTrack(Track track) {
        String template = "Create a vibrant and eye-catching album cover for a music track titled "
            + "'{trackName}' by the artist '{artistName}'. "
            + "The design should resemble the original cover art. "
            + "Ensure the text is legible and complements the overall design.";
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Map<String, Object> variables = Map.of(
            "trackName", track.getTrackName(),
            "artistName", track.getArtistName()
        );
        return generateImage(promptTemplate.create(variables)
            .getContents());
    }

    public String generateImage(String instructions) {
        return imageModel.call(new ImagePrompt(instructions, OpenAiImageOptions.builder()
                .N(1)
                .quality("standard")
                .build()))
            .getResult()
            .getOutput()
            .getUrl();
    }
}
