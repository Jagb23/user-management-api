package com.example.usermanagement.service;

import com.example.usermanagement.dto.UserCreateRequest;
import com.example.usermanagement.dto.UserResponse;
import com.example.usermanagement.dto.UserUpdateRequest;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for user management operations.
 * Handles business logic, validation, and transaction management.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user from the provided request.
     * Validates email uniqueness before creation.
     * 
     * @param request the user creation request
     * @return UserResponse containing the created user data
     * @throws DataIntegrityViolationException if email already exists
     */
    public UserResponse createUser(UserCreateRequest request) {
        // Validate email uniqueness if email is provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DataIntegrityViolationException("User with email '" + request.getEmail() + "' already exists");
            }
        }

        // Create new user entity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail() != null && !request.getEmail().trim().isEmpty() ? request.getEmail() : null);
        
        // Set additional fields if provided
        if (request.getAdditionalFields() != null) {
            user.setAdditionalFields(new HashMap<>(request.getAdditionalFields()));
        }

        // Save and return response
        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Retrieves a user by ID.
     * 
     * @param id the user ID
     * @return UserResponse containing the user data
     * @throws EntityNotFoundException if user is not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    /**
     * Retrieves all users with pagination support.
     * 
     * @param pageable pagination parameters
     * @return Page of UserResponse objects
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(UserResponse::fromEntity);
    }

    /**
     * Updates an existing user with partial data.
     * Preserves existing fields not included in the update request.
     * Validates email uniqueness for updates.
     * 
     * @param id the user ID to update
     * @param request the update request with partial data
     * @return UserResponse containing the updated user data
     * @throws EntityNotFoundException if user is not found
     * @throws DataIntegrityViolationException if email already exists for another user
     */
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        // Find existing user
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        // Validate email uniqueness if email is being updated
        if (request.hasEmail() && request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new DataIntegrityViolationException("User with email '" + request.getEmail() + "' already exists");
            }
        }

        // Update fields that are present in the request
        if (request.hasName()) {
            existingUser.setName(request.getName());
        }

        if (request.hasEmail()) {
            existingUser.setEmail(request.getEmail() != null && !request.getEmail().trim().isEmpty() ? request.getEmail() : null);
        }

        // Handle additional fields merging
        if (request.hasAdditionalFields()) {
            Map<String, Object> existingFields = existingUser.getAdditionalFields();
            if (existingFields == null) {
                existingFields = new HashMap<>();
            }
            
            // Merge new fields with existing ones
            Map<String, Object> updatedFields = new HashMap<>(existingFields);
            if (request.getAdditionalFields() != null) {
                updatedFields.putAll(request.getAdditionalFields());
            }
            existingUser.setAdditionalFields(updatedFields);
        }

        // Save and return response
        User updatedUser = userRepository.save(existingUser);
        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * Deletes a user by ID.
     * 
     * @param id the user ID to delete
     * @throws EntityNotFoundException if user is not found
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Checks if a user exists by ID.
     * 
     * @param id the user ID to check
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Finds a user by email address.
     * 
     * @param email the email address to search for
     * @return Optional containing UserResponse if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponse::fromEntity);
    }

    /**
     * Checks if a user exists with the given email.
     * 
     * @param email the email address to check
     * @return true if user exists with this email, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}