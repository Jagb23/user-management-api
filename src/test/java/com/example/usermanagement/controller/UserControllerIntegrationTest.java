package com.example.usermanagement.controller;

import com.example.usermanagement.dto.UserCreateRequest;
import com.example.usermanagement.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserController POST endpoint.
 * Uses real Spring context and database for testing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createUser_WithValidData_ShouldReturnCreatedUser() {
        // Given
        UserCreateRequest request = new UserCreateRequest("John Doe", "john@example.com", null);

        // When
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", request, UserResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("John Doe");
        assertThat(response.getBody().getEmail()).isEqualTo("john@example.com");
        assertThat(response.getBody().getCreatedAt()).isNotNull();
        assertThat(response.getBody().getUpdatedAt()).isNotNull();
    }

    @Test
    void createUser_WithValidDataAndAdditionalFields_ShouldReturnCreatedUser() {
        // Given
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("department", "Engineering");
        additionalFields.put("age", 30);

        UserCreateRequest request = new UserCreateRequest("Jane Smith", "jane@example.com", additionalFields);

        // When
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", request, UserResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Jane Smith");
        assertThat(response.getBody().getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getBody().getAdditionalFields().get("department")).isEqualTo("Engineering");
        assertThat(response.getBody().getAdditionalFields().get("age")).isEqualTo(30);
    }

    @Test
    void createUser_WithoutEmail_ShouldReturnCreatedUser() {
        // Given
        UserCreateRequest request = new UserCreateRequest("Bob Wilson", null, null);

        // When
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", request, UserResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Bob Wilson");
        assertThat(response.getBody().getEmail()).isNull();
    }

    @Test
    void createUser_WithEmptyName_ShouldReturnBadRequest() {
        // Given
        UserCreateRequest request = new UserCreateRequest("", "test@example.com", null);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Validation failed");
    }

    @Test
    void createUser_WithNullName_ShouldReturnBadRequest() {
        // Given
        UserCreateRequest request = new UserCreateRequest(null, "test@example.com", null);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Validation failed");
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturnBadRequest() {
        // Given
        UserCreateRequest request = new UserCreateRequest("John Doe", "invalid-email", null);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Validation failed");
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldReturnConflict() {
        // Given - Create first user
        UserCreateRequest firstRequest = new UserCreateRequest("John Doe", "duplicate@example.com", null);
        ResponseEntity<UserResponse> firstResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", firstRequest, UserResponse.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // When - Try to create second user with same email
        UserCreateRequest secondRequest = new UserCreateRequest("Jane Doe", "duplicate@example.com", null);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", secondRequest, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(409);
        assertThat(response.getBody().get("error")).isEqualTo("Conflict");
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        // Given - Create a user first
        UserCreateRequest createRequest = new UserCreateRequest("John Doe", "john@example.com", null);
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", createRequest, UserResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long userId = createResponse.getBody().getId();

        // When
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/users/" + userId, UserResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(userId);
        assertThat(response.getBody().getName()).isEqualTo("John Doe");
        assertThat(response.getBody().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getUserById_WithNonExistentId_ShouldReturnNotFound() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/users/999", Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("User not found with id: 999");
    }

    @Test
    void getAllUsers_WithoutPagination_ShouldReturnAllUsers() {
        // Given - Create some users first
        UserCreateRequest user1 = new UserCreateRequest("John Doe", "john@example.com", null);
        UserCreateRequest user2 = new UserCreateRequest("Jane Smith", "jane@example.com", null);
        
        restTemplate.postForEntity("http://localhost:" + port + "/api/users", user1, UserResponse.class);
        restTemplate.postForEntity("http://localhost:" + port + "/api/users", user2, UserResponse.class);

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/users", Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("content")).isNotNull();
        assertThat(response.getBody().get("totalElements")).isEqualTo(2);
        assertThat(response.getBody().get("totalPages")).isEqualTo(1);
        assertThat(response.getBody().get("size")).isEqualTo(20); // Default page size
        assertThat(response.getBody().get("number")).isEqualTo(0); // First page
    }

    @Test
    void getAllUsers_WithPagination_ShouldReturnPagedResults() {
        // Given - Create multiple users
        for (int i = 1; i <= 5; i++) {
            UserCreateRequest user = new UserCreateRequest("User " + i, "user" + i + "@example.com", null);
            restTemplate.postForEntity("http://localhost:" + port + "/api/users", user, UserResponse.class);
        }

        // When - Request page 1 with size 2
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/users?page=1&size=2", Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("content")).isNotNull();
        assertThat(response.getBody().get("totalElements")).isEqualTo(5);
        assertThat(response.getBody().get("totalPages")).isEqualTo(3);
        assertThat(response.getBody().get("size")).isEqualTo(2);
        assertThat(response.getBody().get("number")).isEqualTo(1); // Second page (0-indexed)
    }

    @Test
    void getAllUsers_WithSorting_ShouldReturnSortedResults() {
        // Given - Create users with different names
        UserCreateRequest userZ = new UserCreateRequest("Zoe Wilson", "zoe@example.com", null);
        UserCreateRequest userA = new UserCreateRequest("Alice Brown", "alice@example.com", null);
        
        restTemplate.postForEntity("http://localhost:" + port + "/api/users", userZ, UserResponse.class);
        restTemplate.postForEntity("http://localhost:" + port + "/api/users", userA, UserResponse.class);

        // When - Request sorted by name ascending
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/users?sort=name,asc", Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> content = (java.util.List<Map<String, Object>>) response.getBody().get("content");
        assertThat(content).hasSize(2);
        assertThat(content.get(0).get("name")).isEqualTo("Alice Brown");
        assertThat(content.get(1).get("name")).isEqualTo("Zoe Wilson");
    }

    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedUser() {
        // Given - Create a user first
        UserCreateRequest createRequest = new UserCreateRequest("John Doe", "john@example.com", null);
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", createRequest, UserResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long userId = createResponse.getBody().getId();

        // When - Update the user
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "John Smith");
        updateData.put("email", "johnsmith@example.com");

        restTemplate.put("http://localhost:" + port + "/api/users/" + userId, updateData);

        // Then - Verify the update
        ResponseEntity<UserResponse> getResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/users/" + userId, UserResponse.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("John Smith");
        assertThat(getResponse.getBody().getEmail()).isEqualTo("johnsmith@example.com");
    }

    @Test
    void updateUser_WithPartialData_ShouldPreserveExistingFields() {
        // Given - Create a user with additional fields
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("department", "Engineering");
        additionalFields.put("age", 30);
        
        UserCreateRequest createRequest = new UserCreateRequest("Jane Doe", "jane@example.com", additionalFields);
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", createRequest, UserResponse.class);
        Long userId = createResponse.getBody().getId();

        // When - Update only the name
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "Jane Smith");

        restTemplate.put("http://localhost:" + port + "/api/users/" + userId, updateData);

        // Then - Verify name is updated but other fields are preserved
        ResponseEntity<UserResponse> getResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/users/" + userId, UserResponse.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("Jane Smith");
        assertThat(getResponse.getBody().getEmail()).isEqualTo("jane@example.com"); // Preserved
        assertThat(getResponse.getBody().getAdditionalFields().get("department")).isEqualTo("Engineering"); // Preserved
        assertThat(getResponse.getBody().getAdditionalFields().get("age")).isEqualTo(30); // Preserved
    }

    @Test
    void updateUser_WithNonExistentId_ShouldReturnNotFound() {
        // Given
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "Non Existent");

        // When & Then
        try {
            restTemplate.put("http://localhost:" + port + "/api/users/999", updateData);
        } catch (Exception e) {
            // RestTemplate throws exception for 4xx/5xx responses
            // Let's use exchange method instead to get the response
        }

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/999",
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(updateData),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
    }

    @Test
    void updateUser_WithDuplicateEmail_ShouldReturnConflict() {
        // Given - Create two users
        UserCreateRequest user1 = new UserCreateRequest("User One", "user1@example.com", null);
        UserCreateRequest user2 = new UserCreateRequest("User Two", "user2@example.com", null);
        
        restTemplate.postForEntity("http://localhost:" + port + "/api/users", user1, UserResponse.class);
        ResponseEntity<UserResponse> user2Response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", user2, UserResponse.class);
        Long user2Id = user2Response.getBody().getId();

        // When - Try to update user2 with user1's email
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("email", "user1@example.com");

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + user2Id,
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(updateData),
                Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("status")).isEqualTo(409);
        assertThat(response.getBody().get("error")).isEqualTo("Conflict");
    }

    @Test
    void deleteUser_WithValidId_ShouldReturnNoContent() {
        // Given - Create a user first
        UserCreateRequest createRequest = new UserCreateRequest("John Doe", "john@example.com", null);
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/users", createRequest, UserResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long userId = createResponse.getBody().getId();

        // When - Delete the user
        restTemplate.delete("http://localhost:" + port + "/api/users/" + userId);

        // Then - Verify the user is deleted by trying to get it
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/users/" + userId, Map.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteUser_WithNonExistentId_ShouldReturnNotFound() {
        // When & Then
        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/999",
                org.springframework.http.HttpMethod.DELETE,
                null,
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("User not found with id: 999");
    }
}