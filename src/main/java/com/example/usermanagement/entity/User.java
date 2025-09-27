package com.example.usermanagement.entity;

import com.example.usermanagement.converter.JsonAttributeConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * User entity representing a user in the system with extensible fields.
 * Supports flexible schema through JSON-stored additional fields.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Column(nullable = false)
    private String name;

    @Email(message = "Email must be valid")
    @Column(nullable = true, unique = true)
    private String email;

    @Column(name = "additional_fields", columnDefinition = "TEXT")
    @Convert(converter = JsonAttributeConverter.class)
    private Map<String, Object> additionalFields = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Default constructor
    public User() {}

    // Constructor with required fields
    public User(String name) {
        this.name = name;
        this.additionalFields = new HashMap<>();
    }

    // Constructor with name and email
    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.additionalFields = new HashMap<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Object> getAdditionalFields() {
        return additionalFields != null ? additionalFields : new HashMap<>();
    }

    public void setAdditionalFields(Map<String, Object> additionalFields) {
        this.additionalFields = additionalFields != null ? additionalFields : new HashMap<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods for additional fields
    public void addAdditionalField(String key, Object value) {
        if (this.additionalFields == null) {
            this.additionalFields = new HashMap<>();
        }
        this.additionalFields.put(key, value);
    }

    public Object getAdditionalField(String key) {
        return this.additionalFields != null ? this.additionalFields.get(key) : null;
    }

    public void removeAdditionalField(String key) {
        if (this.additionalFields != null) {
            this.additionalFields.remove(key);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
               Objects.equals(name, user.name) &&
               Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", additionalFields=" + additionalFields +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}