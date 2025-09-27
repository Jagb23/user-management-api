package com.example.usermanagement.controller;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users");
    }

    @Test
    void handleValidationErrors_ShouldReturnBadRequestWithFieldErrors() {
        // Given
        FieldError fieldError1 = new FieldError("userCreateRequest", "name", "Name must not be blank");
        FieldError fieldError2 = new FieldError("userCreateRequest", "email", "Email should be valid");

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler
                .handleValidationErrors(methodArgumentNotValidException, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("timestamp")).isInstanceOf(LocalDateTime.class);
        assertThat(body.get("status")).isEqualTo(400);
        assertThat(body.get("error")).isEqualTo("Bad Request");
        assertThat(body.get("message")).isEqualTo("Validation failed");
        assertThat(body.get("path")).isEqualTo("/api/users");

        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) body.get("details");
        assertThat(details).hasSize(2);
        assertThat(details.get("name")).isEqualTo("Name must not be blank");
        assertThat(details.get("email")).isEqualTo("Email should be valid");
    }

    @Test
    void handleEntityNotFound_ShouldReturnNotFoundWithMessage() {
        // Given
        String errorMessage = "User not found with id: 123";
        EntityNotFoundException exception = new EntityNotFoundException(errorMessage);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler
                .handleEntityNotFound(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("timestamp")).isInstanceOf(LocalDateTime.class);
        assertThat(body.get("status")).isEqualTo(404);
        assertThat(body.get("error")).isEqualTo("Not Found");
        assertThat(body.get("message")).isEqualTo(errorMessage);
        assertThat(body.get("path")).isEqualTo("/api/users");
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturnConflictWithMessage() {
        // Given
        String errorMessage = "could not execute statement; SQL [n/a]; constraint [null]; nested exception is org.hibernate.exception.ConstraintViolationException";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(errorMessage);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler
                .handleDataIntegrityViolation(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("timestamp")).isInstanceOf(LocalDateTime.class);
        assertThat(body.get("status")).isEqualTo(409);
        assertThat(body.get("error")).isEqualTo("Conflict");
        assertThat(body.get("message")).isEqualTo(errorMessage);
        assertThat(body.get("path")).isEqualTo("/api/users");
    }

    @Test
    void handleMalformedJson_ShouldReturnBadRequestWithMessage() {
        // Given
        String errorMessage = "JSON parse error: Unexpected character";
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(errorMessage);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler
                .handleMalformedJson(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("timestamp")).isInstanceOf(LocalDateTime.class);
        assertThat(body.get("status")).isEqualTo(400);
        assertThat(body.get("error")).isEqualTo("Bad Request");
        assertThat(body.get("message")).isEqualTo("Malformed JSON request");
        assertThat(body.get("path")).isEqualTo("/api/users");
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerErrorWithGenericMessage() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected database error");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler
                .handleGenericException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("timestamp")).isInstanceOf(LocalDateTime.class);
        assertThat(body.get("status")).isEqualTo(500);
        assertThat(body.get("error")).isEqualTo("Internal Server Error");
        assertThat(body.get("message")).isEqualTo("An unexpected error occurred");
        assertThat(body.get("path")).isEqualTo("/api/users");
    }

    @Test
    void handleValidationErrors_WithEmptyFieldErrors_ShouldReturnEmptyDetails() {
        // Given
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler
                .handleValidationErrors(methodArgumentNotValidException, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) body.get("details");
        assertThat(details).isEmpty();
    }

    @Test
    void handleEntityNotFound_WithNullMessage_ShouldHandleGracefully() {
        // Given
        EntityNotFoundException exception = new EntityNotFoundException((String) null);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler
                .handleEntityNotFound(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message")).isNull();
    }

    @Test
    void allHandlers_ShouldIncludeTimestamp() {
        // Test that all handlers include a timestamp in their response
        LocalDateTime beforeTest = LocalDateTime.now().minusSeconds(1);

        // Test validation error handler
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> validationResponse = globalExceptionHandler
                .handleValidationErrors(methodArgumentNotValidException, webRequest);

        LocalDateTime validationTimestamp = (LocalDateTime) validationResponse.getBody().get("timestamp");
        assertThat(validationTimestamp).isAfter(beforeTest);

        // Test entity not found handler
        ResponseEntity<Map<String, Object>> notFoundResponse = globalExceptionHandler
                .handleEntityNotFound(new EntityNotFoundException("test"), webRequest);

        LocalDateTime notFoundTimestamp = (LocalDateTime) notFoundResponse.getBody().get("timestamp");
        assertThat(notFoundTimestamp).isAfter(beforeTest);

        // Test data integrity handler
        ResponseEntity<Map<String, Object>> conflictResponse = globalExceptionHandler
                .handleDataIntegrityViolation(new DataIntegrityViolationException("test"), webRequest);

        LocalDateTime conflictTimestamp = (LocalDateTime) conflictResponse.getBody().get("timestamp");
        assertThat(conflictTimestamp).isAfter(beforeTest);

        // Test malformed JSON handler
        ResponseEntity<Map<String, Object>> malformedResponse = globalExceptionHandler
                .handleMalformedJson(new HttpMessageNotReadableException("test"), webRequest);

        LocalDateTime malformedTimestamp = (LocalDateTime) malformedResponse.getBody().get("timestamp");
        assertThat(malformedTimestamp).isAfter(beforeTest);

        // Test generic exception handler
        ResponseEntity<Map<String, Object>> genericResponse = globalExceptionHandler
                .handleGenericException(new RuntimeException("test"), webRequest);

        LocalDateTime genericTimestamp = (LocalDateTime) genericResponse.getBody().get("timestamp");
        assertThat(genericTimestamp).isAfter(beforeTest);
    }
}