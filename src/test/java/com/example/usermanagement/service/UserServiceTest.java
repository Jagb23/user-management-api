package com.example.usermanagement.service;

import com.example.usermanagement.dto.UserCreateRequest;
import com.example.usermanagement.dto.UserResponse;
import com.example.usermanagement.dto.UserUpdateRequest;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService class.
 * Tests business logic and transaction management with mocked repository layer.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setAdditionalFields(Map.of("department", "Engineering"));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // Create test requests
        createRequest = new UserCreateRequest();
        createRequest.setName("Jane Smith");
        createRequest.setEmail("jane@example.com");
        createRequest.setAdditionalFields(Map.of("role", "Developer"));

        updateRequest = new UserUpdateRequest();
        updateRequest.setName("Jane Updated");
        updateRequest.setEmail("jane.updated@example.com");
    }

    @Test
    @DisplayName("Should create user successfully with all fields")
    void createUser_WithAllFields_ShouldReturnUserResponse() {
        // Given
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.createUser(createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        
        verify(userRepository).existsByEmail("jane@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should create user successfully without email")
    void createUser_WithoutEmail_ShouldReturnUserResponse() {
        // Given
        createRequest.setEmail(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.createUser(createRequest);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should create user successfully with empty email")
    void createUser_WithEmptyEmail_ShouldReturnUserResponse() {
        // Given
        createRequest.setEmail("");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.createUser(createRequest);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with duplicate email")
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("User with email 'jane@example.com' already exists");
        
        verify(userRepository).existsByEmail("jane@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void getUserById_WithValidId_ShouldReturnUserResponse() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getUserById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when getting user with invalid ID")
    void getUserById_WithInvalidId_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
        
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void getAllUsers_WithPagination_ShouldReturnPagedResults() {
        // Given
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        // When
        Page<UserResponse> response = userService.getAllUsers(PageRequest.of(0, 10));

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(response.getTotalElements()).isEqualTo(1);
        
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should update user successfully with all fields")
    void updateUser_WithAllFields_ShouldReturnUpdatedUserResponse() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Jane Updated");
        updatedUser.setEmail("jane.updated@example.com");
        updatedUser.setAdditionalFields(Map.of("department", "Engineering"));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("jane.updated@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        UserResponse response = userService.updateUser(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Jane Updated");
        assertThat(response.getEmail()).isEqualTo("jane.updated@example.com");
        
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("jane.updated@example.com", 1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user with partial fields only")
    void updateUser_WithPartialFields_ShouldPreserveExistingFields() {
        // Given
        UserUpdateRequest partialRequest = new UserUpdateRequest();
        partialRequest.setName("Updated Name Only");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.updateUser(1L, partialRequest);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should merge additional fields during update")
    void updateUser_WithAdditionalFields_ShouldMergeFields() {
        // Given
        Map<String, Object> newFields = Map.of("role", "Senior Developer", "location", "Remote");
        updateRequest.setAdditionalFields(newFields);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("jane.updated@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Verify that additional fields were merged
            assertThat(savedUser.getAdditionalFields()).containsEntry("department", "Engineering");
            assertThat(savedUser.getAdditionalFields()).containsEntry("role", "Senior Developer");
            assertThat(savedUser.getAdditionalFields()).containsEntry("location", "Remote");
            return savedUser;
        });

        // When
        userService.updateUser(1L, updateRequest);

        // Then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating user with duplicate email")
    void updateUser_WithDuplicateEmail_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("jane.updated@example.com", 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(1L, updateRequest))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("User with email 'jane.updated@example.com' already exists");
        
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("jane.updated@example.com", 1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    void updateUser_WithInvalidId_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(999L, updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
        
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void deleteUser_WithInvalidId_ShouldThrowException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
        
        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should check if user exists by ID")
    void existsById_WithValidId_ShouldReturnTrue() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        boolean exists = userService.existsById(1L);

        // Then
        assertThat(exists).isTrue();
        verify(userRepository).existsById(1L);
    }

    @Test
    @DisplayName("Should find user by email")
    void findByEmail_WithValidEmail_ShouldReturnUserResponse() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<UserResponse> response = userService.findByEmail("john@example.com");

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getEmail()).isEqualTo("john@example.com");
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should return empty when finding user by non-existent email")
    void findByEmail_WithInvalidEmail_ShouldReturnEmpty() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        Optional<UserResponse> response = userService.findByEmail("nonexistent@example.com");

        // Then
        assertThat(response).isEmpty();
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void existsByEmail_WithValidEmail_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // When
        boolean exists = userService.existsByEmail("john@example.com");

        // Then
        assertThat(exists).isTrue();
        verify(userRepository).existsByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should handle null additional fields in create request")
    void createUser_WithNullAdditionalFields_ShouldCreateUserSuccessfully() {
        // Given
        createRequest.setAdditionalFields(null);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.createUser(createRequest);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle user with null additional fields during update")
    void updateUser_WithUserHavingNullAdditionalFields_ShouldHandleGracefully() {
        // Given
        testUser.setAdditionalFields(null);
        updateRequest.setAdditionalFields(Map.of("newField", "newValue"));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("jane.updated@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertThat(savedUser.getAdditionalFields()).containsEntry("newField", "newValue");
            return savedUser;
        });

        // When
        userService.updateUser(1L, updateRequest);

        // Then
        verify(userRepository).save(any(User.class));
    }
}