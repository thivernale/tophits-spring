package org.thivernale.tophits.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thivernale.tophits.ai.tools.DateTimeTools;
import org.thivernale.tophits.ai.tools.WeatherTools;

import java.net.InetAddress;

@RestController
@RequestMapping("/api/tools")
class ToolsAugmentedController {
    private static final Logger log = LoggerFactory.getLogger(ToolsAugmentedController.class);
    private final ChatClient chatClient;
    private final WeatherTools weatherTools;

    ToolsAugmentedController(ChatClient chatClient, WeatherTools weatherTools) {
        this.chatClient = chatClient.mutate()
            .defaultTools(new DateTimeTools())
            .build();
        this.weatherTools = weatherTools;
    }

    @GetMapping("/datetime")
    public String getRequestedDateTime() {
        return chatClient.prompt()
            .user("What date is next Monday?")
            .call()
            .content();
    }

    @GetMapping("/weather")
    public String getRequestedWeather(
        @RequestParam(defaultValue = "What will the weather be like next Monday?") String message,
        HttpServletRequest request
    ) {
        String ipAddr = request.getHeader("X-Forwarded-For");
        if (ipAddr == null || ipAddr.isEmpty()) {
            ipAddr = request.getRemoteAddr();
        }
        if (ipAddr.equals(getLoopbackAddress())) {
            ipAddr = weatherTools.getMyIpAddress();
        }

        return chatClient.prompt()
            .user("What date and day of the week is today? %s My public IP address is %s".formatted(message, ipAddr))
            .tools(weatherTools)
            .call()
            .content();
    }

    private String getLoopbackAddress() {
        return InetAddress.getLoopbackAddress()
            .getHostAddress();
    }
}
