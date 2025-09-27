# Design Document

## Overview

The User Management API is a Spring Boot REST service that provides CRUD operations for user entities. The system uses a layered architecture with clear separation of concerns, leveraging Spring's dependency injection and auto-configuration capabilities. The design emphasizes extensibility through a flexible data model that can accommodate future field additions without breaking existing functionality.

## Architecture

The application follows a standard Spring Boot layered architecture:

```
┌─────────────────┐
│   REST Layer    │ ← Controllers, DTOs, Validation
├─────────────────┤
│  Service Layer  │ ← Business Logic, Transactions
├─────────────────┤
│Repository Layer │ ← Data Access, JPA Repositories
├─────────────────┤
│   Data Layer    │ ← SQLite Database, Entities
└─────────────────┘
```

### Key Architectural Decisions

1. **Spring Boot**: Provides auto-configuration, embedded server, and production-ready features
2. **Spring Data JPA**: Simplifies database operations with repository pattern
3. **SQLite**: Embedded database for simplicity and portability
4. **JSON Flexible Fields**: Uses JSON column type to store additional user attributes
5. **DTO Pattern**: Separates API contracts from internal data models

## Components and Interfaces

### REST Controllers

**UserController**
- Handles HTTP requests for user operations
- Maps to `/api/users` base path
- Implements standard REST conventions
- Validates input using Bean Validation annotations

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    // GET /api/users - List all users with pagination
    // GET /api/users/{id} - Get user by ID
    // POST /api/users - Create new user
    // PUT /api/users/{id} - Update existing user
    // DELETE /api/users/{id} - Delete user
}
```

### Service Layer

**UserService**
- Contains business logic for user operations
- Handles validation and business rules
- Manages transactions
- Converts between DTOs and entities

### Repository Layer

**UserRepository**
- Extends JpaRepository for basic CRUD operations
- Custom queries for email uniqueness validation
- Handles database interactions

### DTOs (Data Transfer Objects)

**UserCreateRequest**
- Validates required fields (name only)
- Email is optional but must be unique when provided
- Accepts additional fields as Map<String, Object>

**UserUpdateRequest**
- Similar to create but allows partial updates
- Preserves existing fields not included in request

**UserResponse**
- Returns user data including ID and all fields
- Consistent response format

## Data Models

### User Entity

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = true, unique = true)
    private String email;
    
    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonAttributeConverter.class)
    private Map<String, Object> additionalFields;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Database Schema

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE, -- Optional but unique when provided
    additional_fields TEXT, -- JSON column for extensible fields
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
```

### Extensible Field Design

The system uses a JSON column (`additional_fields`) to store dynamic user attributes:
- Allows adding new fields without schema changes
- Maintains backward compatibility
- Supports complex data types (nested objects, arrays)
- Indexed for basic queries if needed

## Error Handling

### Global Exception Handler

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    // Handles validation errors (400)
    // Handles resource not found (404)
    // Handles duplicate email (409)
    // Handles database errors (500)
    // Handles generic server errors (500)
}
```

### Error Response Format

```json
{
    "timestamp": "2025-01-15T10:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "path": "/api/users",
    "details": {
        "name": "Name must not be empty"
    }
}
```

### HTTP Status Code Mapping

- **200 OK**: Successful GET, PUT operations
- **201 Created**: Successful POST operations
- **204 No Content**: Successful DELETE operations
- **400 Bad Request**: Validation errors, malformed JSON
- **404 Not Found**: Resource not found
- **409 Conflict**: Duplicate email address
- **500 Internal Server Error**: Unexpected server errors
- **503 Service Unavailable**: Database connectivity issues

## Testing Strategy

### Unit Tests
- **Controller Tests**: Mock service layer, test HTTP mappings and validation
- **Service Tests**: Mock repository layer, test business logic
- **Repository Tests**: Use @DataJpaTest with in-memory database

### Integration Tests
- **API Tests**: Full HTTP request/response cycle testing
- **Database Tests**: Test actual SQLite integration
- **End-to-End Tests**: Complete user workflows

### Test Data Management
- Use TestContainers or embedded H2 for integration tests
- Factory pattern for test data creation
- Separate test profiles for different environments

### Testing Tools
- **JUnit 5**: Primary testing framework
- **Mockito**: Mocking framework
- **Spring Boot Test**: Integration testing support
- **TestContainers**: Database integration testing
- **AssertJ**: Fluent assertions

## Configuration

### Application Properties

```yaml
spring:
  datasource:
    url: jdbc:sqlite:users.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    database-platform: org.hibernate.community.dialect.SQLiteDialect
  
server:
  port: 8080
  
logging:
  level:
    com.example.usermanagement: INFO
    org.springframework.web: DEBUG
```

### Maven Dependencies

Key dependencies include:
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- sqlite-jdbc
- hibernate-community-dialects
- spring-boot-starter-validation
- spring-boot-starter-test