package com.example.usermanagement.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity validation and field mapping.
 */
@DisplayName("User Entity Tests")
class UserTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create user with valid name")
    void shouldCreateUserWithValidName() {
        // Given
        String name = "John Doe";
        
        // When
        User user = new User(name);
        
        // Then
        assertNotNull(user);
        assertEquals(name, user.getName());
        assertNull(user.getEmail());
        assertNotNull(user.getAdditionalFields());
        assertTrue(user.getAdditionalFields().isEmpty());
    }

    @Test
    @DisplayName("Should create user with name and email")
    void shouldCreateUserWithNameAndEmail() {
        // Given
        String name = "Jane Smith";
        String email = "jane.smith@example.com";
        
        // When
        User user = new User(name, email);
        
        // Then
        assertNotNull(user);
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertNotNull(user.getAdditionalFields());
        assertTrue(user.getAdditionalFields().isEmpty());
    }

    @Test
    @DisplayName("Should validate that name is not blank")
    void shouldValidateNameNotBlank() {
        // Given
        User user = new User();
        user.setName("");
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Name must not be blank")));
    }

    @Test
    @DisplayName("Should validate that name is not null")
    void shouldValidateNameNotNull() {
        // Given
        User user = new User();
        user.setName(null);
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Name must not be blank")));
    }

    @Test
    @DisplayName("Should validate email format when provided")
    void shouldValidateEmailFormat() {
        // Given
        User user = new User("John Doe");
        user.setEmail("invalid-email");
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must be valid")));
    }

    @Test
    @DisplayName("Should accept valid email format")
    void shouldAcceptValidEmailFormat() {
        // Given
        User user = new User("John Doe");
        user.setEmail("john.doe@example.com");
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should accept null email")
    void shouldAcceptNullEmail() {
        // Given
        User user = new User("John Doe");
        user.setEmail(null);
        
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should handle additional fields correctly")
    void shouldHandleAdditionalFields() {
        // Given
        User user = new User("John Doe");
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("department", "Engineering");
        additionalFields.put("age", 30);
        additionalFields.put("skills", java.util.Arrays.asList("Java", "Spring", "SQL"));
        
        // When
        user.setAdditionalFields(additionalFields);
        
        // Then
        assertEquals(additionalFields, user.getAdditionalFields());
        assertEquals("Engineering", user.getAdditionalField("department"));
        assertEquals(30, user.getAdditionalField("age"));
        assertNotNull(user.getAdditionalField("skills"));
    }

    @Test
    @DisplayName("Should add individual additional fields")
    void shouldAddIndividualAdditionalFields() {
        // Given
        User user = new User("Jane Smith");
        
        // When
        user.addAdditionalField("department", "Marketing");
        user.addAdditionalField("level", "Senior");
        
        // Then
        assertEquals("Marketing", user.getAdditionalField("department"));
        assertEquals("Senior", user.getAdditionalField("level"));
        assertEquals(2, user.getAdditionalFields().size());
    }

    @Test
    @DisplayName("Should remove additional fields")
    void shouldRemoveAdditionalFields() {
        // Given
        User user = new User("Bob Johnson");
        user.addAdditionalField("department", "Sales");
        user.addAdditionalField("level", "Junior");
        
        // When
        user.removeAdditionalField("level");
        
        // Then
        assertEquals("Sales", user.getAdditionalField("department"));
        assertNull(user.getAdditionalField("level"));
        assertEquals(1, user.getAdditionalFields().size());
    }

    @Test
    @DisplayName("Should handle null additional fields gracefully")
    void shouldHandleNullAdditionalFields() {
        // Given
        User user = new User("Alice Brown");
        
        // When
        user.setAdditionalFields(null);
        
        // Then
        assertNotNull(user.getAdditionalFields());
        assertTrue(user.getAdditionalFields().isEmpty());
        assertNull(user.getAdditionalField("nonexistent"));
    }

    @Test
    @DisplayName("Should initialize additional fields when adding to null map")
    void shouldInitializeAdditionalFieldsWhenAddingToNullMap() {
        // Given
        User user = new User("Charlie Wilson");
        user.setAdditionalFields(null);
        
        // When
        user.addAdditionalField("location", "New York");
        
        // Then
        assertNotNull(user.getAdditionalFields());
        assertEquals("New York", user.getAdditionalField("location"));
        assertEquals(1, user.getAdditionalFields().size());
    }

    @Test
    @DisplayName("Should handle timestamps correctly")
    void shouldHandleTimestamps() {
        // Given
        User user = new User("David Lee");
        LocalDateTime now = LocalDateTime.now();
        
        // When
        user.setCreatedAt(now);
        user.setUpdatedAt(now.plusMinutes(5));
        
        // Then
        assertEquals(now, user.getCreatedAt());
        assertEquals(now.plusMinutes(5), user.getUpdatedAt());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        User user1 = new User("John Doe", "john@example.com");
        user1.setId(1L);
        
        User user2 = new User("John Doe", "john@example.com");
        user2.setId(1L);
        
        User user3 = new User("Jane Smith", "jane@example.com");
        user3.setId(2L);
        
        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertNotEquals(user1, null);
        assertNotEquals(user1, "not a user");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        User user1 = new User("John Doe", "john@example.com");
        user1.setId(1L);
        
        User user2 = new User("John Doe", "john@example.com");
        user2.setId(1L);
        
        // Then
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        User user = new User("John Doe", "john@example.com");
        user.setId(1L);
        user.addAdditionalField("department", "IT");
        
        // When
        String toString = user.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("John Doe"));
        assertTrue(toString.contains("john@example.com"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("department=IT"));
    }

    @Test
    @DisplayName("Should handle complex additional field types")
    void shouldHandleComplexAdditionalFieldTypes() {
        // Given
        User user = new User("Complex User");
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("street", "123 Main St");
        nestedMap.put("city", "Anytown");
        
        // When
        user.addAdditionalField("address", nestedMap);
        user.addAdditionalField("active", true);
        user.addAdditionalField("score", 95.5);
        
        // Then
        assertEquals(nestedMap, user.getAdditionalField("address"));
        assertEquals(true, user.getAdditionalField("active"));
        assertEquals(95.5, user.getAdditionalField("score"));
    }
}