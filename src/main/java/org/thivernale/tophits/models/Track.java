package org.thivernale.tophits.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
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
    private Integer bpm = null;

    @Size(max = 10)
    @Column("key_signature")
    private String keySignature = null;

    @Size(max = 10)
    @Column("mode")
    private String mode = null;

    @Column("danceability_percent")
    private Integer danceabilityPercent = null;

    @Column("valence_percent")
    private Integer valencePercent = null;

    @Column("energy_percent")
    private Integer energyPercent = null;

    @Column("acousticness_percent")
    private Integer acousticnessPercent = null;

    @Column("instrumentalness_percent")
    private Integer instrumentalnessPercent = null;

    @Column("liveness_percent")
    private Integer livenessPercent = null;

    @Column("speechiness_percent")
    private Integer speechinessPercent = null;
}
