package com.example.usermanagement.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * JPA AttributeConverter for converting Map<String, Object> to JSON string and vice versa.
 * This converter enables storing flexible, extensible fields as JSON in the database.
 */
@Converter
public class JsonAttributeConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final Logger logger = LoggerFactory.getLogger(JsonAttributeConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts Map<String, Object> to JSON string for database storage.
     *
     * @param attribute the Map to convert
     * @return JSON string representation, or null if input is null or empty
     */
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            logger.error("Error converting Map to JSON string", e);
            throw new RuntimeException("Error converting Map to JSON", e);
        }
    }

    /**
     * Converts JSON string from database to Map<String, Object>.
     *
     * @param dbData the JSON string from database
     * @return Map<String, Object> representation, or empty map if input is null or empty
     */
    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSON string to Map", e);
            throw new RuntimeException("Error converting JSON to Map", e);
        }
    }
}