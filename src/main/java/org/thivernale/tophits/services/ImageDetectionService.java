package org.thivernale.tophits.services;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageDetectionService {

    private final ChatClient chatClient;

    public String imageToText(Resource sampleImage) {
        return chatClient.prompt()
            .user((userSpec) -> {
                userSpec.text("Describe the content of this image in detail.");
                userSpec.media(MediaType.IMAGE_PNG, sampleImage);
            })
            .call()
            .content();
    }
}
