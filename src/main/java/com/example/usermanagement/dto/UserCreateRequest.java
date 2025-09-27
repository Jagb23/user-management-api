package com.example.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * DTO for creating new users.
 * Contains validation for required fields and optional email.
 */
public class UserCreateRequest {
    
    @NotBlank(message = "Name must not be empty")
    private String name;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private Map<String, Object> additionalFields;
    
    public UserCreateRequest() {}
    
    public UserCreateRequest(String name, String email, Map<String, Object> additionalFields) {
        this.name = name;
        this.email = email;
        this.additionalFields = additionalFields;
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
}