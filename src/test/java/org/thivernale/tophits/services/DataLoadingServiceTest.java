package org.thivernale.tophits.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thivernale.tophits.models.Track;
import org.thivernale.tophits.repositories.TrackRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataLoadingServiceTest {
    @Mock
    private TrackRepository trackRepo;
    private DataLoadingService dataLoadingService;

    @BeforeEach
    void setUp() {
        dataLoadingService = new DataLoadingService(trackRepo, null);
        // ReflectionTestUtils.setField(dataLoadingService, "trackRepository", null);
    }

    @Test
    void listFiles() {
        assertThatNoException().isThrownBy(() -> {
            List<String> dataFiles = dataLoadingService.getAvailableCsvFiles();

            assertThat(dataFiles).isNotEmpty()
                .isNotNull()
                .size()
                .isEqualTo(1);
            assertThat(dataFiles.getFirst()).isEqualTo("spotify-2023.csv");
        });
    }

    @Test
    void loadCsvFile() {
        when(trackRepo.save(any(Track.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatNoException().isThrownBy(() -> {
            var loadResult = dataLoadingService.loadCsvFile("spotify-2023.csv");

            assertThat(loadResult.successCount()).isGreaterThan(0);
            assertThat(loadResult.errorCount()).isZero();
        });
    }

    @ParameterizedTest(name = "Parsing CSV line: {0}")
    @ValueSource(strings = {
        "track_name,artist(s)_name,artist_count,released_year,released_month,released_day,in_spotify_playlists,in_spotify_charts,streams,in_apple_playlists,in_apple_charts,in_deezer_playlists,in_deezer_charts,in_shazam_charts,bpm,key,mode,danceability_%,valence_%,energy_%,acousticness_%,instrumentalness_%,liveness_%,speechiness_%",
        "BESO,\"Rauw Alejandro, ROSALï¿½\",2,2023,3,24,4053,50,357925728,82,121,182,12,171,95,F,Minor,77,53,64,74,0,17,14",
        "\"Song with comma, in title\",\"Artist, Name\",1,2022,5,15,1000,20,12345678,30,40,50,5,60,120,G,Major,80,60,70,50,10,20,5"
    })
    void parseCsvLine(String line) {
        assertThat(dataLoadingService.parseCsvLine(line)).hasSize(24);
    }
}
