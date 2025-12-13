-- track_name,artist(s)_name,artist_count,
-- released_year,released_month,released_day,
-- in_spotify_playlists,in_spotify_charts,streams,
-- in_apple_playlists,in_apple_charts,in_deezer_playlists,
-- in_deezer_charts,in_shazam_charts,bpm,key,mode,
-- danceability_%,valence_%,energy_%,acousticness_%,
-- instrumentalness_%,liveness_%,speechiness_%
COPY tracks (
             track_name,
             artist_name,
--              artist_count,
             released_year,
             released_month,
             released_day,
             in_spotify_playlists,
             in_spotify_charts,
             streams,
             in_apple_playlists,
             in_apple_charts,
             in_deezer_playlists,
             in_deezer_charts,
             in_shazam_charts,
             bpm,
             key_signature,
             mode,
             danceability_percent,
             valence_percent,
             energy_percent,
             acousticness_percent,
             instrumentalness_percent,
             liveness_percent,
             speechiness_percent
    )
    FROM '/home/data.csv'
    WITH (FORMAT CSV, DELIMITER ',', HEADER true);

-- ------------------------------
