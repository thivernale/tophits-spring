package org.thivernale.tophits.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

public interface VectorDocGenerator<T> {
    ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

    String generateSemanticRepresentation(T item);

    default Map<String, Object> generateMetadata(T item) {
        return objectMapper.convertValue(item, new TypeReference<>() {
        });
    }
}
