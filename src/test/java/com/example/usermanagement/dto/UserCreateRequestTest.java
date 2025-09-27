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

class UserCreateRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void shouldPassValidationWithValidNameAndEmail() {
        // Given
        UserCreateRequest request = new UserCreateRequest("John Doe", "john@example.com", null);
        
        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    void shouldPassValidationWithValidNameAndNoEmail() {
        // Given
        UserCreateRequest request = new UserCreateRequest("John Doe", null, null);
        
        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    void shouldPassValidationWithValidNameEmptyEmailAndAdditionalFields() {
        // Given
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("age", 30);
        additionalFields.put("department", "Engineering");
        
        UserCreateRequest request = new UserCreateRequest("John Doe", "", additionalFields);
        
        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getAdditionalFields()).containsEntry("age", 30);
        assertThat(request.getAdditionalFields()).containsEntry("department", "Engineering");
    }
    
    @Test
    void shouldFailValidationWhenNameIsNull() {
        // Given
        UserCreateRequest request = new UserCreateRequest(null, "john@example.com", null);
        
        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name must not be empty");
    }
    
    @Test
    void shouldFailValidationWhenNameIsEmpty() {
        // Given
        UserCreateRequest request = new UserCreateRequest("", "john@example.com", null);
        
        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name must not be empty");
    }
    
    @Test
    void shouldFailValidationWhenNameIsBlank() {
        // Given
        UserCreateRequest request = new UserCreateRequest("   ", "john@example.com", null);
        
        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name must not be empty");
    }
    
    @Test
    void shouldFailValidationWhenEmailIsInvalid() {
        // Given
        UserCreateRequest request = new UserCreateRequest("John Doe", "invalid-email", null);
        
        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email must be valid");
    }
    
    @Test
    void shouldFailValidationWhenBothNameAndEmailAreInvalid() {
        // Given
        UserCreateRequest request = new UserCreateRequest("", "invalid-email", null);
        
        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(2);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder("Name must not be empty", "Email must be valid");
    }
}