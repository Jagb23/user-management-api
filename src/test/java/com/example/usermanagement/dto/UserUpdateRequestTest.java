package com.example.usermanagement.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserUpdateRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void shouldPassValidationWithAllFieldsValid() {
        // Given
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("age", 30);
        UserUpdateRequest request = new UserUpdateRequest("John Doe", "john@example.com", additionalFields);
        
        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(request.hasName()).isTrue();
        assertThat(request.hasEmail()).isTrue();
        assertThat(request.hasAdditionalFields()).isTrue();
    }
    
    @Test
    void shouldPassValidationWithOnlyNameProvided() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest("John Doe", null, null);
        
        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(request.hasName()).isTrue();
        assertThat(request.hasEmail()).isFalse();
        assertThat(request.hasAdditionalFields()).isFalse();
    }
    
    @Test
    void shouldPassValidationWithOnlyEmailProvided() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest(null, "john@example.com", null);
        
        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(request.hasName()).isFalse();
        assertThat(request.hasEmail()).isTrue();
        assertThat(request.hasAdditionalFields()).isFalse();
    }
    
    @Test
    void shouldPassValidationWithOnlyAdditionalFieldsProvided() {
        // Given
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("department", "Engineering");
        UserUpdateRequest request = new UserUpdateRequest(null, null, additionalFields);
        
        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(request.hasName()).isFalse();
        assertThat(request.hasEmail()).isFalse();
        assertThat(request.hasAdditionalFields()).isTrue();
    }
    
    @Test
    void shouldPassValidationWithEmptyName() {
        // Given - Empty name should be allowed for updates (user might want to clear it)
        UserUpdateRequest request = new UserUpdateRequest("", "john@example.com", null);
        
        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(request.hasName()).isTrue();
    }
    
    @Test
    void shouldPassValidationWithEmptyEmail() {
        // Given - Empty email should be allowed for updates (user might want to clear it)
        UserUpdateRequest request = new UserUpdateRequest("John Doe", "", null);
        
        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(request.hasEmail()).isTrue();
    }
    
    @Test
    void shouldPassValidationWithAllFieldsNull() {
        // Given - All null fields should be valid for partial updates
        UserUpdateRequest request = new UserUpdateRequest(null, null, null);
        
        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(request.hasName()).isFalse();
        assertThat(request.hasEmail()).isFalse();
        assertThat(request.hasAdditionalFields()).isFalse();
    }
    
    @Test
    void shouldFailValidationWhenEmailIsInvalid() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest("John Doe", "invalid-email", null);
        
        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email must be valid");
    }
    
    @Test
    void shouldHandleComplexAdditionalFields() {
        // Given
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("age", 30);
        additionalFields.put("skills", java.util.Arrays.asList("Java", "Spring", "SQL"));
        additionalFields.put("address", Map.of("city", "New York", "country", "USA"));
        
        UserUpdateRequest request = new UserUpdateRequest(null, null, additionalFields);
        
        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getAdditionalFields()).containsEntry("age", 30);
        assertThat(request.getAdditionalFields()).containsKey("skills");
        assertThat(request.getAdditionalFields()).containsKey("address");
    }
}