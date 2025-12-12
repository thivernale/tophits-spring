package org.thivernale.tophits.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
@Table(name = "bass_line_cache")
public class BassLineCache {
    @Id
    private Long id;

    private String artistName;

    private String trackName;

    private String bassLineContent;

    private Instant createdAt;

    private Instant updatedAt;


}
