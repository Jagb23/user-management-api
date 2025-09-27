package com.example.usermanagement.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JsonAttributeConverter Tests")
class JsonAttributeConverterTest {

    private JsonAttributeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JsonAttributeConverter();
    }

    @Test
    @DisplayName("Should convert simple Map to JSON string")
    void shouldConvertSimpleMapToJson() {
        // Given
        Map<String, Object> map = new HashMap<>();
        map.put("age", 25);
        map.put("department", "Engineering");
        map.put("active", true);

        // When
        String json = converter.convertToDatabaseColumn(map);

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("\"age\":25");
        assertThat(json).contains("\"department\":\"Engineering\"");
        assertThat(json).contains("\"active\":true");
    }

    @Test
    @DisplayName("Should convert complex Map with nested objects to JSON string")
    void shouldConvertComplexMapToJson() {
        // Given
        Map<String, Object> map = new HashMap<>();
        map.put("age", 30);
        map.put("skills", Arrays.asList("Java", "Spring", "SQL"));
        
        Map<String, Object> address = new HashMap<>();
        address.put("street", "123 Main St");
        address.put("city", "Springfield");
        map.put("address", address);

        // When
        String json = converter.convertToDatabaseColumn(map);

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("\"age\":30");
        assertThat(json).contains("\"skills\":[\"Java\",\"Spring\",\"SQL\"]");
        assertThat(json).contains("\"address\":{");
        assertThat(json).contains("\"street\":\"123 Main St\"");
        assertThat(json).contains("\"city\":\"Springfield\"");
    }

    @Test
    @DisplayName("Should return null when converting null Map to JSON")
    void shouldReturnNullForNullMap() {
        // When
        String json = converter.convertToDatabaseColumn(null);

        // Then
        assertThat(json).isNull();
    }

    @Test
    @DisplayName("Should return null when converting empty Map to JSON")
    void shouldReturnNullForEmptyMap() {
        // Given
        Map<String, Object> emptyMap = new HashMap<>();

        // When
        String json = converter.convertToDatabaseColumn(emptyMap);

        // Then
        assertThat(json).isNull();
    }

    @Test
    @DisplayName("Should convert JSON string to Map")
    void shouldConvertJsonToMap() {
        // Given
        String json = "{\"age\":25,\"department\":\"Engineering\",\"active\":true}";

        // When
        Map<String, Object> map = converter.convertToEntityAttribute(json);

        // Then
        assertThat(map).isNotNull();
        assertThat(map).hasSize(3);
        assertThat(map.get("age")).isEqualTo(25);
        assertThat(map.get("department")).isEqualTo("Engineering");
        assertThat(map.get("active")).isEqualTo(true);
    }

    @Test
    @DisplayName("Should convert complex JSON with nested objects to Map")
    void shouldConvertComplexJsonToMap() {
        // Given
        String json = "{\"age\":30,\"skills\":[\"Java\",\"Spring\",\"SQL\"],\"address\":{\"street\":\"123 Main St\",\"city\":\"Springfield\"}}";

        // When
        Map<String, Object> map = converter.convertToEntityAttribute(json);

        // Then
        assertThat(map).isNotNull();
        assertThat(map).hasSize(3);
        assertThat(map.get("age")).isEqualTo(30);
        
        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) map.get("skills");
        assertThat(skills).containsExactly("Java", "Spring", "SQL");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> address = (Map<String, Object>) map.get("address");
        assertThat(address.get("street")).isEqualTo("123 Main St");
        assertThat(address.get("city")).isEqualTo("Springfield");
    }

    @Test
    @DisplayName("Should return empty Map when converting null JSON string")
    void shouldReturnEmptyMapForNullJson() {
        // When
        Map<String, Object> map = converter.convertToEntityAttribute(null);

        // Then
        assertThat(map).isNotNull();
        assertThat(map).isEmpty();
    }

    @Test
    @DisplayName("Should return empty Map when converting empty JSON string")
    void shouldReturnEmptyMapForEmptyJson() {
        // When
        Map<String, Object> map = converter.convertToEntityAttribute("");

        // Then
        assertThat(map).isNotNull();
        assertThat(map).isEmpty();
    }

    @Test
    @DisplayName("Should return empty Map when converting whitespace-only JSON string")
    void shouldReturnEmptyMapForWhitespaceJson() {
        // When
        Map<String, Object> map = converter.convertToEntityAttribute("   ");

        // Then
        assertThat(map).isNotNull();
        assertThat(map).isEmpty();
    }

    @Test
    @DisplayName("Should throw RuntimeException for invalid JSON when converting to Map")
    void shouldThrowExceptionForInvalidJson() {
        // Given
        String invalidJson = "{invalid json}";

        // When & Then
        assertThatThrownBy(() -> converter.convertToEntityAttribute(invalidJson))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error converting JSON to Map");
    }

    @Test
    @DisplayName("Should handle round-trip conversion correctly")
    void shouldHandleRoundTripConversion() {
        // Given
        Map<String, Object> originalMap = new HashMap<>();
        originalMap.put("name", "John Doe");
        originalMap.put("age", 35);
        originalMap.put("isManager", true);
        originalMap.put("salary", 75000.50);

        // When - convert to JSON and back
        String json = converter.convertToDatabaseColumn(originalMap);
        Map<String, Object> convertedMap = converter.convertToEntityAttribute(json);

        // Then
        assertThat(convertedMap).isNotNull();
        assertThat(convertedMap).hasSize(4);
        assertThat(convertedMap.get("name")).isEqualTo("John Doe");
        assertThat(convertedMap.get("age")).isEqualTo(35);
        assertThat(convertedMap.get("isManager")).isEqualTo(true);
        assertThat(convertedMap.get("salary")).isEqualTo(75000.5); // JSON numbers are doubles
    }

    @Test
    @DisplayName("Should handle Map with null values")
    void shouldHandleMapWithNullValues() {
        // Given
        Map<String, Object> map = new HashMap<>();
        map.put("name", "John");
        map.put("middleName", null);
        map.put("age", 30);

        // When
        String json = converter.convertToDatabaseColumn(map);
        Map<String, Object> convertedMap = converter.convertToEntityAttribute(json);

        // Then
        assertThat(convertedMap).isNotNull();
        assertThat(convertedMap).hasSize(3);
        assertThat(convertedMap.get("name")).isEqualTo("John");
        assertThat(convertedMap.get("middleName")).isNull();
        assertThat(convertedMap.get("age")).isEqualTo(30);
    }
}