package com.example.usermanagement.dto;

import com.example.usermanagement.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {
    
    @Test
    void shouldConvertUserEntityToUserResponse() {
        // Given
        User user = new User("John Doe", "john@example.com");
        user.setId(1L);
        
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("age", 30);
        additionalFields.put("department", "Engineering");
        user.setAdditionalFields(additionalFields);
        
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // When
        UserResponse response = UserResponse.fromEntity(user);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getAdditionalFields()).containsEntry("age", 30);
        assertThat(response.getAdditionalFields()).containsEntry("department", "Engineering");
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }
    
    @Test
    void shouldConvertUserEntityWithNullEmailToUserResponse() {
        // Given
        User user = new User("Jane Doe");
        user.setId(2L);
        user.setEmail(null);
        
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // When
        UserResponse response = UserResponse.fromEntity(user);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Jane Doe");
        assertThat(response.getEmail()).isNull();
        assertThat(response.getAdditionalFields()).isNotNull();
        assertThat(response.getAdditionalFields()).isEmpty();
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }
    
    @Test
    void shouldConvertUserEntityWithEmptyAdditionalFieldsToUserResponse() {
        // Given
        User user = new User("Bob Smith", "bob@example.com");
        user.setId(3L);
        user.setAdditionalFields(new HashMap<>());
        
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // When
        UserResponse response = UserResponse.fromEntity(user);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getName()).isEqualTo("Bob Smith");
        assertThat(response.getEmail()).isEqualTo("bob@example.com");
        assertThat(response.getAdditionalFields()).isNotNull();
        assertThat(response.getAdditionalFields()).isEmpty();
    }
    
    @Test
    void shouldConvertUserEntityWithNullAdditionalFieldsToUserResponse() {
        // Given
        User user = new User("Alice Johnson", "alice@example.com");
        user.setId(4L);
        user.setAdditionalFields(null);
        
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // When
        UserResponse response = UserResponse.fromEntity(user);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(4L);
        assertThat(response.getName()).isEqualTo("Alice Johnson");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getAdditionalFields()).isNotNull();
        assertThat(response.getAdditionalFields()).isEmpty();
    }
    
    @Test
    void shouldReturnNullWhenConvertingNullUserEntity() {
        // Given
        User user = null;
        
        // When
        UserResponse response = UserResponse.fromEntity(user);
        
        // Then
        assertThat(response).isNull();
    }
    
    @Test
    void shouldCreateDeepCopyOfAdditionalFields() {
        // Given
        User user = new User("Test User", "test@example.com");
        user.setId(5L);
        
        Map<String, Object> originalFields = new HashMap<>();
        originalFields.put("skill", "Java");
        originalFields.put("level", "Senior");
        user.setAdditionalFields(originalFields);
        
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // When
        UserResponse response = UserResponse.fromEntity(user);
        
        // Modify the original additional fields
        originalFields.put("newSkill", "Spring");
        
        // Then
        assertThat(response.getAdditionalFields()).doesNotContainKey("newSkill");
        assertThat(response.getAdditionalFields()).containsEntry("skill", "Java");
        assertThat(response.getAdditionalFields()).containsEntry("level", "Senior");
        assertThat(response.getAdditionalFields()).hasSize(2);
    }
    
    @Test
    void shouldHandleComplexAdditionalFields() {
        // Given
        User user = new User("Complex User", "complex@example.com");
        user.setId(6L);
        
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("age", 35);
        additionalFields.put("skills", java.util.Arrays.asList("Java", "Spring", "SQL"));
        additionalFields.put("address", Map.of("city", "New York", "country", "USA"));
        additionalFields.put("isActive", true);
        user.setAdditionalFields(additionalFields);
        
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // When
        UserResponse response = UserResponse.fromEntity(user);
        
        // Then
        assertThat(response.getAdditionalFields()).containsEntry("age", 35);
        assertThat(response.getAdditionalFields()).containsKey("skills");
        assertThat(response.getAdditionalFields()).containsKey("address");
        assertThat(response.getAdditionalFields()).containsEntry("isActive", true);
        assertThat(response.getAdditionalFields()).hasSize(4);
    }
}