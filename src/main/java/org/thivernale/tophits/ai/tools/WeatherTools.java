package org.thivernale.tophits.ai.tools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherTools {
    private final GeoIpTool geoIpTool;
    private final WeatherForecastTool weatherForecastTool;

    //    @Tool(description = "Get my IP address")
    public String getMyIpAddress() {
        return geoIpTool.getIpAddress();
    }

    @Tool(description = "Get latitude, longitude, timezone by public IP address")
    public String geoIpFields(
        @ToolParam(description = "IP address to get latitude, longitude, timezone for") String ip) {
        return geoIpTool.getGeoIpFields(ip)
            .toString();
    }

    @Tool(description = "Get weather forecast for specific latitude, longitude, timezone")
    public String getWeatherForecast(
        @ToolParam(description = "Latitude") double lon,
        @ToolParam(description = "Longitude") double lat,
        @ToolParam(description = "Timezone") String timezone) {
        return weatherForecastTool.getWeatherForecastByLocation(new GeoIpFields("", lat, lon, timezone));
    }

    @Component
    static class WeatherForecastTool {
        private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
        private final RestClient restClient;

        public WeatherForecastTool(RestClient.Builder builder) {
            this.restClient = builder
                .baseUrl(BASE_URL)
                .requestInterceptor((request, body, execution) -> {
                    log.info("Requesting URI: {}", request.getURI());
                    return execution.execute(request, body);
                })
                .build();
        }

        public String getWeatherForecastByLocation(GeoIpFields geoIpFields) {
            var uriVariables = Map.of(
                "latitude", geoIpFields.lat,
                "longitude", geoIpFields.lon,
                "timezone", geoIpFields.timezone,
                "daily", "temperature_2m_min,temperature_2m_max,weather_code"
            );

            var forecast = restClient.get()
                .uri("?latitude={latitude}&longitude={longitude}&timezone={timezone}&daily={daily}", uriVariables)
                .retrieve()
                .body(Forecast.class);
            assert forecast != null;
            return formatForecast(forecast);
        }

        private String formatForecast(Forecast forecast) {
            StringBuilder builder = new StringBuilder();
            builder.append("Weather forecast for the next %s days%n%n".formatted(forecast.daily.time.size()));

            String collected = IntStream.range(0, forecast.daily.time.size())
                .mapToObj(i -> String.format("""
                        Date: %s
                        Min. temperature: %s %s
                        Max. temperature: %s %s
                        Description: %s
                        """,
                    forecast.daily.time.get(i),
                    forecast.daily.temperature2mMin.get(i),
                    forecast.dailyUnits.temperature2mMin,
                    forecast.daily.temperature2mMax.get(i),
                    forecast.dailyUnits.temperature2mMax,
                    getWeatherDescription(forecast.daily.weatherCode.get(i))
                ))
                .collect(Collectors.joining());
            builder.append(collected);

            return builder.toString();
        }

        private String getWeatherDescription(short code) {
            return switch (code) {
                case 0 -> "Clear sky";
                case 1, 2, 3 -> "Mainly clear, partly cloudy, and overcast";
                case 45, 48 -> "Fog and depositing rime fog";
                case 51, 53, 55 -> "Drizzle: Light, moderate, and dense intensity";
                case 56, 57 -> "Freezing Drizzle: Light and dense intensity";
                case 61, 63, 65 -> "Rain: Slight, moderate and heavy intensity";
                case 66, 67 -> "Freezing Rain: Light and heavy intensity";
                case 71, 73, 75 -> "Snow fall: Slight, moderate, and heavy intensity";
                case 77 -> "Snow grains";
                case 80, 81, 82 -> "Rain showers: Slight, moderate, and violent";
                case 85, 86 -> "Snow showers: Slight and heavy";
                default -> "Unknown weather code";
            };
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeoIpFields(
        @JsonProperty("query") String ip,
        double lat,
        double lon,
        String timezone
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Forecast(
        DailyUnits dailyUnits,
        Daily daily
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
        public record DailyUnits(
            String time,
            @JsonProperty("temperature_2m_min") String temperature2mMin,
            @JsonProperty("temperature_2m_max") String temperature2mMax
        ) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
        public record Daily(
            List<String> time,
            @JsonProperty("temperature_2m_min") List<Double> temperature2mMin,
            @JsonProperty("temperature_2m_max") List<Double> temperature2mMax,
            List<Short> weatherCode
        ) {
        }
    }

    @Component
    static class GeoIpTool {
        private static final String BASE_URL = "http://ip-api.com/json/";
        private final RestClient restClient;

        public GeoIpTool(RestClient.Builder clientBuilder) {
            restClient = clientBuilder.baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        }

        public GeoIpFields getGeoIpFields(String ip) {
            if (Objects.isNull(ip)) {
                ip = "";
            }
            String[] fields = {"query", "lon", "lat", "timezone"};
            return restClient.get()
                .uri("{ip}?fields={fields}", ip, String.join(",", fields))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(GeoIpFields.class);
        }

        public String getIpAddress() {
            return restClient.get()
                .uri("https://checkip.amazonaws.com")
                .retrieve()
                .body(String.class);
        }
    }
}
