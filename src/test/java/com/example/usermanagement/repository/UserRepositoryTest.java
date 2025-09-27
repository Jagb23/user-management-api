package com.example.usermanagement.repository;

import com.example.usermanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for UserRepository using @DataJpaTest.
 * Tests repository operations with embedded database.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        // Create test users with different scenarios
        testUser1 = new User("John Doe", "john.doe@example.com");
        Map<String, Object> additionalFields1 = new HashMap<>();
        additionalFields1.put("department", "Engineering");
        additionalFields1.put("level", "Senior");
        testUser1.setAdditionalFields(additionalFields1);

        testUser2 = new User("Jane Smith", "jane.smith@example.com");
        Map<String, Object> additionalFields2 = new HashMap<>();
        additionalFields2.put("department", "Marketing");
        additionalFields2.put("level", "Manager");
        testUser2.setAdditionalFields(additionalFields2);

        // User without email
        testUser3 = new User("Bob Wilson");
        Map<String, Object> additionalFields3 = new HashMap<>();
        additionalFields3.put("department", "Sales");
        testUser3.setAdditionalFields(additionalFields3);
    }

    @Test
    @DisplayName("Should save and find user by ID")
    void shouldSaveAndFindUserById() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
        assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(foundUser.get().getAdditionalFields()).containsEntry("department", "Engineering");
        assertThat(foundUser.get().getCreatedAt()).isNotNull();
        assertThat(foundUser.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findByEmail("jane.smith@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Jane Smith");
        assertThat(foundUser.get().getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // Given
        entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void shouldCheckIfUserExistsByEmail() {
        // Given
        entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // When & Then
        assertThat(userRepository.existsByEmail("john.doe@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should check email uniqueness excluding specific user ID")
    void shouldCheckEmailUniquenessExcludingUserId() {
        // Given
        User savedUser1 = entityManager.persistAndFlush(testUser1);
        User savedUser2 = entityManager.persistAndFlush(testUser2);
        entityManager.clear();

        // When & Then
        // Should return false when checking the same user's email against their own ID
        assertThat(userRepository.existsByEmailAndIdNot("john.doe@example.com", savedUser1.getId())).isFalse();
        
        // Should return true when checking if another user has the same email
        assertThat(userRepository.existsByEmailAndIdNot("john.doe@example.com", savedUser2.getId())).isTrue();
        
        // Should return false for non-existent email
        assertThat(userRepository.existsByEmailAndIdNot("nonexistent@example.com", savedUser1.getId())).isFalse();
    }

    @Test
    @DisplayName("Should find user by name ignoring case")
    void shouldFindUserByNameIgnoreCase() {
        // Given
        entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // When
        Optional<User> foundUser1 = userRepository.findByNameIgnoreCase("john doe");
        Optional<User> foundUser2 = userRepository.findByNameIgnoreCase("JOHN DOE");
        Optional<User> foundUser3 = userRepository.findByNameIgnoreCase("John Doe");

        // Then
        assertThat(foundUser1).isPresent();
        assertThat(foundUser2).isPresent();
        assertThat(foundUser3).isPresent();
        assertThat(foundUser1.get().getName()).isEqualTo("John Doe");
        assertThat(foundUser2.get().getName()).isEqualTo("John Doe");
        assertThat(foundUser3.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should check if user exists by name ignoring case")
    void shouldCheckIfUserExistsByNameIgnoreCase() {
        // Given
        entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // When & Then
        assertThat(userRepository.existsByNameIgnoreCase("john doe")).isTrue();
        assertThat(userRepository.existsByNameIgnoreCase("JOHN DOE")).isTrue();
        assertThat(userRepository.existsByNameIgnoreCase("John Doe")).isTrue();
        assertThat(userRepository.existsByNameIgnoreCase("Jane Smith")).isFalse();
    }

    @Test
    @DisplayName("Should handle users without email")
    void shouldHandleUsersWithoutEmail() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser3);
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Bob Wilson");
        assertThat(foundUser.get().getEmail()).isNull();
        assertThat(foundUser.get().getAdditionalFields()).containsEntry("department", "Sales");
    }

    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {
        // Given
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        entityManager.persistAndFlush(testUser3);
        entityManager.clear();

        // When
        var allUsers = userRepository.findAll();

        // Then
        assertThat(allUsers).hasSize(3);
        assertThat(allUsers).extracting(User::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith", "Bob Wilson");
    }

    @Test
    @DisplayName("Should delete user by ID")
    void shouldDeleteUserById() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // When
        userRepository.deleteById(savedUser.getId());

        // Then
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Should count users correctly")
    void shouldCountUsersCorrectly() {
        // Given
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        entityManager.clear();

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle null email in exists check")
    void shouldHandleNullEmailInExistsCheck() {
        // Given
        entityManager.persistAndFlush(testUser1); // User with email
        entityManager.clear();

        // When & Then
        // Should return false for null email check when no users have null email
        assertThat(userRepository.existsByEmail(null)).isFalse();
        
        // Should return true for existing email
        assertThat(userRepository.existsByEmail("john.doe@example.com")).isTrue();
    }

    @Test
    @DisplayName("Should preserve additional fields during persistence")
    void shouldPreserveAdditionalFieldsDuringPersistence() {
        // Given
        Map<String, Object> complexFields = new HashMap<>();
        complexFields.put("skills", java.util.List.of("Java", "Spring", "SQL"));
        complexFields.put("experience", 5);
        complexFields.put("remote", true);
        complexFields.put("metadata", Map.of("lastLogin", "2025-01-15", "preferences", Map.of("theme", "dark")));
        
        testUser1.setAdditionalFields(complexFields);
        User savedUser = entityManager.persistAndFlush(testUser1);
        entityManager.clear();

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        Map<String, Object> retrievedFields = foundUser.get().getAdditionalFields();
        assertThat(retrievedFields).containsEntry("skills", java.util.List.of("Java", "Spring", "SQL"));
        assertThat(retrievedFields).containsEntry("experience", 5);
        assertThat(retrievedFields).containsEntry("remote", true);
        assertThat(retrievedFields).containsKey("metadata");
    }
}