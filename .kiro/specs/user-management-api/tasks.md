# Implementation Plan

- [x] 1. Set up project structure and Maven configuration
  - Create Maven project with Spring Boot parent
  - Configure pom.xml with required dependencies (Spring Boot Web, Data JPA, SQLite, Validation, Test)
  - Set up application.yml with SQLite configuration
  - Create package structure for controllers, services, repositories, entities, and DTOs
  - _Requirements: 7.2, 7.3_

- [x] 2. Implement core data models and JSON converter
  - Create JsonAttributeConverter class for handling Map<String, Object> to JSON conversion
  - Write unit tests for JsonAttributeConverter to ensure proper serialization/deserialization
  - _Requirements: 5.1, 5.3_

- [x] 3. Create User entity with extensible fields
  - Implement User JPA entity with id, name, optional email, additionalFields, and timestamps
  - Configure JPA annotations for nullable email with unique constraint
  - Write unit tests for User entity validation and field mapping
  - _Requirements: 1.1, 5.1, 5.2_

- [x] 4. Implement repository layer
  - Create UserRepository interface extending JpaRepository
  - Add custom query methods for finding by email and checking email uniqueness
  - Write repository tests using @DataJpaTest with embedded database
  - _Requirements: 2.1, 2.3, 3.4, 4.2_

- [x] 5. Create DTO classes for API contracts
- [x] 5.1 Implement UserCreateRequest DTO
  - Create DTO with name validation, optional email, and additionalFields Map
  - Add Bean Validation annotations (@NotBlank for name, @Email for email when present)
  - Write unit tests for DTO validation
  - _Requirements: 1.2, 1.3, 5.4_

- [x] 5.2 Implement UserUpdateRequest DTO
  - Create DTO allowing partial updates with optional name, email, and additionalFields
  - Add validation annotations and null-safe field handling
  - Write unit tests for partial update validation
  - _Requirements: 3.1, 3.2, 5.4_

- [x] 5.3 Implement UserResponse DTO
  - Create response DTO with all user fields including dynamic additionalFields
  - Add conversion methods between User entity and UserResponse
  - Write unit tests for entity-to-DTO conversion
  - _Requirements: 2.1, 2.2, 5.3_

- [x] 6. Implement service layer with business logic
  - Create UserService class with CRUD operations
  - Implement email uniqueness validation for create and update operations
  - Add transaction management with @Transactional annotations
  - Handle field merging for updates to preserve existing additionalFields
  - Write comprehensive unit tests mocking repository layer
  - _Requirements: 1.1, 1.4, 2.1, 3.1, 3.4, 4.1, 5.2, 5.4_

- [x] 7. Create REST controller with all endpoints
- [x] 7.1 Implement POST /api/users endpoint
  - Create endpoint for user creation with UserCreateRequest validation
  - Return UserResponse with HTTP 201 status
  - Handle validation errors and duplicate email conflicts
  - Write controller tests using MockMvc
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 7.2 Implement GET /api/users/{id} endpoint
  - Create endpoint for retrieving single user by ID
  - Return UserResponse with HTTP 200 or 404 for not found
  - Write controller tests for success and not found scenarios
  - _Requirements: 2.1, 2.3_

- [x] 7.3 Implement GET /api/users endpoint with pagination
  - Create endpoint for listing all users with Pageable support
  - Return Page<UserResponse> with HTTP 200 status
  - Add pagination parameters (page, size, sort)
  - Write controller tests for pagination functionality
  - _Requirements: 2.2, 2.4_

- [x] 7.4 Implement PUT /api/users/{id} endpoint
  - Create endpoint for updating existing users
  - Handle partial updates preserving existing fields not in request
  - Return updated UserResponse with HTTP 200 or 404 for not found
  - Handle validation errors and duplicate email conflicts
  - Write controller tests for update scenarios
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 7.5 Implement DELETE /api/users/{id} endpoint
  - Create endpoint for deleting users by ID
  - Return HTTP 204 for successful deletion or 404 for not found
  - Write controller tests for delete scenarios
  - _Requirements: 4.1, 4.2_

- [x] 8. Implement global exception handling
  - Create GlobalExceptionHandler with @ControllerAdvice
  - Handle MethodArgumentNotValidException for validation errors (400)
  - Handle EntityNotFoundException for resource not found (404)
  - Handle DataIntegrityViolationException for duplicate email (409)
  - Handle generic exceptions for server errors (500)
  - Create standardized error response format with timestamp, status, message, and details
  - Write unit tests for exception handler methods
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 9. Configure database initialization and properties
  - Set up application.yml with SQLite configuration and JPA settings
  - Configure Hibernate dialect for SQLite
  - Set up database initialization with proper DDL settings
  - Add logging configuration for development and production
  - _Requirements: 7.3, 7.4_

- [ ] 10. Create integration tests for complete API workflows
  - Write integration tests using @SpringBootTest and TestRestTemplate
  - Test complete CRUD workflows including edge cases
  - Test extensible fields functionality with dynamic field additions
  - Test error scenarios and proper HTTP status codes
  - Verify database persistence and retrieval of additional fields
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 5.2, 5.3, 5.4, 6.1, 6.2, 6.3, 6.4_

- [ ] 11. Add application startup and health check
  - Create main Spring Boot application class with @SpringBootApplication
  - Add basic health check endpoint for monitoring
  - Configure application to create database file on startup
  - Write integration test to verify application starts successfully
  - _Requirements: 7.1, 7.2_