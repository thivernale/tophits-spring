package org.thivernale.tophits.services;

import org.thivernale.tophits.models.Track;

import java.util.ArrayList;
import java.util.List;

public class TrackVectorDocGenerator implements VectorDocGenerator<Track> {
    @Override
    public String generateSemanticRepresentation(Track track) {
        return "Track: %s, Artist: %s, Year: %d. Mood: %s. Texture: %s.".formatted(
            track.getTrackName(),
            track.getArtistName(),
            track.getReleasedYear(),
            collectMood(track),
            collectTexture(track));
    }

    private String mapFeature(int value, String low, String mid, String high) {
        return switch (value) {
            case int v when v < 35 -> low;
            case int v when v > 65 -> high;
            default -> mid;
        };
    }

    private String collectMood(Track track) {
        List<String> features = new ArrayList<>();
        features.add(mapFeature(track.getValencePercent(), "melancholy", "balanced-mood", "cheerful"));
        features.add(mapFeature(track.getEnergyPercent(), "mellow", "moderate-energy", "high-energy"));
        features.add(mapFeature(track.getDanceabilityPercent(), "ambient", "rhythmic/groovy", "danceable"));

        return String.join(", ", features);
    }

    private String collectTexture(Track track) {
        List<String> features = new ArrayList<>();
        features.add(mapFeature(track.getAcousticnessPercent(), "electric", "semi-acoustic", "acoustic"));
        features.add(mapFeature(track.getInstrumentalnessPercent(), "vocal-driven", "sparse-vocal", "instrumental-focus"));
        features.add(mapFeature(track.getLivenessPercent(), "studio", "live-ambience", "live"));
        features.add(mapFeature(track.getSpeechinessPercent(), "musical", "mixed-vocal", "spoken-word"));

        return String.join(", ", features);
    }
}
