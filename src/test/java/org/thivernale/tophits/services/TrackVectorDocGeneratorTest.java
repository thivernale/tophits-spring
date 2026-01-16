package org.thivernale.tophits.services;

import org.junit.jupiter.api.Test;
import org.thivernale.tophits.models.Track;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackVectorDocGeneratorTest {

    private final TrackVectorDocGenerator generator = new TrackVectorDocGenerator();

    @Test
    void testSemanticRepresentation_HappyPartySong() {
        // Arrange: A song with low energy, low valence, and low danceability
        Track track = new Track();
        track.setTrackName("Get Lucky - Radio Edit");
        track.setArtistName("Pharrell Williams, Nile Rodgers, Daft Punk");
        track.setReleasedYear(2013);
        track.setValencePercent(87);   // Should be "cheerful"
        track.setEnergyPercent(81);    // Should be "high-energy"
        track.setDanceabilityPercent(79); // Should be "danceable"
        track.setAcousticnessPercent(4);
        track.setInstrumentalnessPercent(0);
        track.setLivenessPercent(10);
        track.setSpeechinessPercent(4);

        // Act
        String result = generator.generateSemanticRepresentation(track);

        // Assert
        assertTrue(result.contains("cheerful"), "Should map high valence to cheerful");
        assertTrue(result.contains("high-energy"), "Should map high energy to high-energy");
        assertTrue(result.contains("danceable"), "Should map high danceability to danceable");
        assertTrue(result.contains("electric"), "Should map low acousticness to electric");
        assertTrue(result.contains("vocal-driven"), "Should map low instrumentalness to vocal-driven");
        assertTrue(result.contains("studio"), "Should map low liveness to studio");
        assertTrue(result.contains("musical"), "Should map low speechiness to musical");

        System.out.println("Generated Text: " + result);
    }

    @Test
    void testSemanticRepresentation_SadChillSong() {
        // Arrange: High energy, high valence, high danceability
        Track track = new Track();
        track.setTrackName("Something In The Way - Remastered 2021");
        track.setArtistName("Nirvana");
        track.setReleasedYear(1991);
        track.setValencePercent(8);   // Should be "melancholy"
        track.setEnergyPercent(20);    // Should be "mellow"
        track.setDanceabilityPercent(44); // Should be "rhythmic/groovy"
        track.setAcousticnessPercent(74); // Should be "acoustic"
        track.setInstrumentalnessPercent(42); // Should be "sparse-vocal"
        track.setLivenessPercent(11); // Should be "studio" - questionable but those are the numbers
        track.setSpeechinessPercent(3); // Should be "musical"

        // Act
        String result = generator.generateSemanticRepresentation(track);

        // Assert
        assertTrue(result.contains("melancholy"));
        assertTrue(result.contains("mellow"));
        assertTrue(result.contains("rhythmic/groovy"));
        assertTrue(result.contains("acoustic"));
        assertTrue(result.contains("sparse-vocal"));
        assertTrue(result.contains("studio"));
        assertTrue(result.contains("musical"));

        System.out.println("Generated Text: " + result);
    }
}
