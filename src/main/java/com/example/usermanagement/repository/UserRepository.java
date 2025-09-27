package com.example.usermanagement.repository;

import com.example.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email address.
     * 
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given email address.
     * 
     * @param email the email address to check
     * @return true if a user exists with this email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user exists with the given email address, excluding a specific user ID.
     * This is useful for update operations to check email uniqueness.
     * 
     * @param email the email address to check
     * @param userId the user ID to exclude from the check
     * @return true if another user exists with this email, false otherwise
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);

    /**
     * Find users by name (case-insensitive).
     * 
     * @param name the name to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByNameIgnoreCase(String name);

    /**
     * Check if a user exists with the given name (case-insensitive).
     * 
     * @param name the name to check
     * @return true if a user exists with this name, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
}