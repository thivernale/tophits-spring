package org.thivernale.tophits.services;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.stereotype.Service;
import org.thivernale.tophits.models.Track;
import org.thivernale.tophits.repositories.TrackRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataLoadingService {
    private static final String DATA_DIRECTORY = "data/";
    private final TrackRepository trackRepository;
    private final Validator validator;

    public List<String> getAvailableCsvFiles() {
        Path path = Path.of(DATA_DIRECTORY);

        if (!Files.exists(path) || !Files.isDirectory(path)) {
            log.warn("Data directory does not exist or is not a directory: {}", DATA_DIRECTORY);
            return Collections.emptyList();
        }

        try (Stream<Path> walk = Files.walk(path)) {
            return walk
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(string -> string.endsWith(".csv"))
                .toList();
        } catch (IOException e) {
            log.warn("Error reading data directory: {}", DATA_DIRECTORY, e);
            return Collections.emptyList();
        }
    }

    public LoadResult loadCsvFile(String fileName) {
        Path path = Path.of(DATA_DIRECTORY)
            .resolve(fileName);

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }

        log.info("Loading file: {}", fileName);
        AtomicInteger index = new AtomicInteger(1);
        List<String> errors = new ArrayList<>();

        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("File is empty: " + fileName);
            }

            List<String> headers = Arrays.stream(headerLine.split(","))
                .toList();
            log.info("Headers: {}", headers);

            reader
                .lines()
                //TODO remove filters below
                //.filter(line -> line.startsWith("BESO"))
                .filter(line -> line.startsWith("ЧЕР")) // test import of Cyrillic named tracks
                .forEach(s -> {
                    index.getAndIncrement();
                    try {
                        List<String> values = parseCsvLine(s);
                        if (values.size() != headers.size()) {
                            log.warn("Skipping line {} due to mismatched number of fields: expected {}, got {}", index.get(), headers.size(), values.size());
                            errors.add("Line " + index.get() + ": Column count mismatch");
                            return;
                        }
                        Track track = createTrackFromCsvData(headers, values);
                        log.info("Values: {}", values);
                        log.info("Track: {}", track);
                        //TODO remove
                        Track savedTrack = trackRepository.save(track);
                        log.info("Saved track with ID: {}", savedTrack.getId());
                    } catch (Exception e) {
                        log.error("Error processing line {}: {}", index, e.getMessage());
                        errors.add("Line " + index.get() + ": " + e.getMessage());
                    }
                });

        } catch (Exception e) {
            log.error("Error processing file: {}", fileName, e);
            throw new RuntimeException("Failed to load CSV file: " + e.getMessage(), e);
        }

        int errorCount = errors.size();
        int successCount = index.get() - 1 - errorCount;

        log.info("Finished loading file: {}. Successfully loaded {} records, {} errors.", fileName, successCount, errorCount);

        return new LoadResult(successCount, errorCount, errors);
    }

    protected List<String> parseCsvLine(String line) {
        int length = line.length();
        int start = 0;
        int end;
        boolean inQuotes = false;
        List<String> fields = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if ((c == ',' || i == length - 1) && !inQuotes) {
                end = (i == length - 1) ? length : i;
                String field = line.substring(start, end)
                    .replaceAll("^\"|\"$", "");
                fields.add(field);
                start = i + 1;
            }
        }
        return fields;
    }

    private Track createTrackFromCsvData(List<String> headers, List<String> values) {
        Track track = new Track();
        Map<String, String> valueMap = IntStream.range(0, values.size())
            .boxed()
            .collect(Collectors.toMap(headers::get, values::get));

        Arrays.stream(Track.class.getDeclaredFields())
            .forEach(field -> {
                String fieldName = field.getName();
                switch (fieldName) {
                    case "id":
                        return;
                    case "keySignature":
                        fieldName = "key";
                        break;
                    case "artistName":
                        fieldName = "artist(s)_name";
                        break;
                    default:
                        Column annotation = field.getDeclaredAnnotation(Column.class);
                        if (annotation != null) {
                            fieldName = annotation.value();
                        }
                        fieldName = fieldName.replaceAll("_percent$", "_%");
                        break;
                }
                try {
                    // set field value using reflection
                    String fieldValue = valueMap.get(fieldName);
                    if (fieldValue == null || fieldValue.isEmpty()) {
                        return;
                    }
                    field.setAccessible(true);
                    if (field.getType()
                        .equals(String.class)) {
                        field.set(track, fieldValue);
                    } else if (field.getType()
                        .equals(Integer.class)) {
                        field.set(track, Integer.parseInt(fieldValue.replaceAll("[^0-9]", "")));
                    } else if (field.getType()
                        .equals(Long.class)) {
                        field.set(track, Long.parseLong(fieldValue.replaceAll("[^0-9]", "")));
                    }
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });

        Set<ConstraintViolation<Track>> violationSet = validator.validate(track);
        if (!violationSet.isEmpty()) {
            String violations = violationSet.stream()
                .map(trackConstraintViolation -> trackConstraintViolation.getPropertyPath()
                    .iterator()
                    .next() +
                    " " +
                    trackConstraintViolation.getMessage())
                .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Validation failed for track: " + violations);
        }

        return track;
    }

    public Track testDatabaseInsertion() {
        Track track = new Track();
        track.setTrackName("Test Track");
        track.setArtistName("Test Artist");
        track.setReleasedYear(2024);
        track.setReleasedMonth(1);
        track.setReleasedDay(1);
        track.setStreams(1_000_000L);

        return trackRepository.save(track);
    }

    public record LoadResult(int successCount, int errorCount, List<String> errors) {
    }
}
