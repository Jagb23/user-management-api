package com.example.usermanagement.dto;

import com.example.usermanagement.entity.User;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for returning user data in API responses.
 * Contains all user fields including dynamic additionalFields.
 */
public class UserResponse {
    
    private Long id;
    private String name;
    private String email;
    private Map<String, Object> additionalFields;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public UserResponse() {}
    
    public UserResponse(Long id, String name, String email, Map<String, Object> additionalFields, 
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.additionalFields = additionalFields;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Converts a User entity to UserResponse DTO.
     * @param user the User entity to convert
     * @return UserResponse DTO
     */
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAdditionalFields() != null ? new HashMap<>(user.getAdditionalFields()) : new HashMap<>(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
    
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
        return additionalFields;
    }
    
    public void setAdditionalFields(Map<String, Object> additionalFields) {
        this.additionalFields = additionalFields;
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
}