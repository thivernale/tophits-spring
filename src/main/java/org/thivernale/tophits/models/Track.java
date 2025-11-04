package org.thivernale.tophits.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "tracks")
public class Track {
    @Id
    @Column("id")
    private Long id;

    @Size(max = 500)
    @NotNull
    @Column("track_name")
    private String trackName;

    @Size(max = 500)
    @NotNull
    @Column("artist_name")
    private String artistName;

    @NotNull
    @Column("released_year")
    private Integer releasedYear;

    @NotNull
    @Column("released_month")
    private Integer releasedMonth;

    @NotNull
    @Column("released_day")
    private Integer releasedDay;

    @NotNull
    @Column("in_spotify_playlists")
    private Integer inSpotifyPlaylists = 0;

    @NotNull
    @Column("in_spotify_charts")
    private Integer inSpotifyCharts = 0;

    @NotNull
    @Column("streams")
    private Long streams = 0L;

    @NotNull
    @Column("in_apple_playlists")
    private Integer inApplePlaylists = 0;

    @NotNull
    @Column("in_apple_charts")
    private Integer inAppleCharts = 0;

    @NotNull
    @Column("in_deezer_playlists")
    private Integer inDeezerPlaylists = 0;

    @NotNull
    @Column("in_deezer_charts")
    private Integer inDeezerCharts = 0;

    @NotNull
    @Column("in_shazam_charts")
    private Integer inShazamCharts = 0;

    @Column("bpm")
    private Integer bpm;

    @Size(max = 10)
    @Column("key_signature")
    private String keySignature;

    @Size(max = 10)
    @Column("mode")
    private String mode;

    @Column("danceability_percent")
    private Integer danceabilityPercent;

    @Column("valence_percent")
    private Integer valencePercent;

    @Column("energy_percent")
    private Integer energyPercent;

    @Column("acousticness_percent")
    private Integer acousticnessPercent;

    @Column("instrumentalness_percent")
    private Integer instrumentalnessPercent;

    @Column("liveness_percent")
    private Integer livenessPercent;

    @Column("speechiness_percent")
    private Integer speechinessPercent;
}
