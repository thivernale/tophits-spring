package org.thivernale.tophits.services;

import org.thivernale.tophits.models.Track;

import java.util.List;

public class TrackVectorDocGenerator implements VectorDocGenerator<Track> {
    @Override
    public String generateSemanticRepresentation(Track track) {
        return "Track %s, Artist %s, Year %d, Vibes: %s.".formatted(
            track.getTrackName(), track.getArtistName(), track.getReleasedYear(), collectVibes(track));
    }

    private String mapVibe(int value, String low, String mid, String high) {
        return switch (value) {
            case int v when v < 35 -> low;
            case int v when v > 65 -> high;
            default -> mid;
        };
    }

    private String collectVibes(Track track) {
        List<String> vibes = new java.util.ArrayList<>();
        vibes.add(mapVibe(track.getValencePercent(), "melancholy", "balanced-mood", "cheerful"));
        vibes.add(mapVibe(track.getEnergyPercent(), "mellow", "moderate-energy", "high-energy"));
        vibes.add(mapVibe(track.getDanceabilityPercent(), "ambient", "rhythmic", "danceable"));

        return String.join(", ", vibes);
    }
}
