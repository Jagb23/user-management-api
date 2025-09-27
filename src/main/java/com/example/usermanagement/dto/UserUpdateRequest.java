package com.example.usermanagement.dto;

import jakarta.validation.constraints.Email;
import java.util.Map;

/**
 * DTO for updating existing users.
 * All fields are optional to support partial updates.
 */
public class UserUpdateRequest {
    
    private String name;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private Map<String, Object> additionalFields;
    
    public UserUpdateRequest() {}
    
    public UserUpdateRequest(String name, String email, Map<String, Object> additionalFields) {
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
    
    /**
     * Checks if the name field is present in the update request.
     * @return true if name is not null, false otherwise
     */
    public boolean hasName() {
        return name != null;
    }
    
    /**
     * Checks if the email field is present in the update request.
     * @return true if email is not null, false otherwise
     */
    public boolean hasEmail() {
        return email != null;
    }
    
    /**
     * Checks if additional fields are present in the update request.
     * @return true if additionalFields is not null, false otherwise
     */
    public boolean hasAdditionalFields() {
        return additionalFields != null;
    }
}