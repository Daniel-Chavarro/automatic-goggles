# Configuration Package Testing Strategy

## Overview
This document outlines the testing strategy for the `org.java_avanzado.taller.config` package. We will use a Hybrid Testing Strategy that leverages `ApplicationContextRunner` for fast, isolated tests of bean creation, and `@SpringBootTest` with an in-memory H2 database for integration and end-to-end (E2E) verification.

## 1. Unit & Isolated Context Tests
These tests focus on bean creation and configuration properties without the overhead of starting the full Spring Boot application context. 

### Technologies
- JUnit 5
- `ApplicationContextRunner` (Spring Boot Test)
- Mockito

### Targets
*   **`AsyncConfiguration`**: 
    *   Use `ApplicationContextRunner` to load the configuration.
    *   Assert that a bean named `taskExecutor` is present.
    *   Assert properties like `ThreadNamePrefix` equals "Tasks-Async-".
*   **`JpaAuditingConfig`**: 
    *   Use `ApplicationContextRunner` to verify it loads without errors and provides the expected Spring Data JPA auditing beans (if explicitly declared).
*   **`OpenApiConfig`**: 
    *   Use `ApplicationContextRunner` to verify the `OpenAPI` bean is created.
    *   Validate expected metadata (e.g., title, version).
*   **`BootstrapDataRunner`**: 
    *   Use plain JUnit + Mockito.
    *   Mock dependencies (`UserRepository`, `RoleRepository`, `PasswordEncoder`, etc.).
    *   Call `run()`.
    *   Verify `repository.save()` methods are invoked the expected number of times without hitting a database.

## 2. Integration Tests
These tests verify that configurations behave correctly in a realistic runtime environment using an in-memory H2 database.

### Technologies
- JUnit 5
- `@SpringBootTest`
- `@AutoConfigureMockMvc`
- H2 In-Memory Database

### Targets
*   **`SecurityConfig`**: 
    *   Use `MockMvc` and `@WithMockUser` to validate endpoint authorization rules.
    *   Verify `permitAll()` paths (`/api/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`).
    *   Verify role-based access for `/api/products/**` (ADMIN vs CLIENT).
    *   Verify role-based access for `/api/users/**` and `/api/audit/**` (ADMIN only).
    *   Verify role-based access for `/api/orders/**` endpoints.
*   **`AsyncConfiguration`**: 
    *   Create a test-scoped `@Async` component.
    *   Invoke it and assert that the executing thread name starts with "Tasks-Async-".
*   **`JpaAuditingConfig`**: 
    *   Save a dummy JPA entity (e.g., User or Product).
    *   Verify that `@CreatedDate` and `@LastModifiedDate` fields are populated automatically by the persistence context.
*   **`OpenApiConfig`**: 
    *   Use `MockMvc` to perform a GET on `/v3/api-docs`.
    *   Assert HTTP 200 OK and a valid JSON structure.
*   **`BootstrapDataRunner`**: 
    *   Start the context with the H2 database.
    *   Query the actual database via repositories to ensure the seed data (e.g., default admin user, roles) is correctly loaded during startup.

## 3. End-to-End (Smoke) Tests
Verifies that all configurations wire together correctly.

### Targets
*   **Context Load Test**: 
    *   A single `@SpringBootTest` class with an empty `@Test void contextLoads()` method.
    *   Validates that the Spring Context starts up cleanly without `BeanCreationException` or conflicting configuration errors when all config classes are active simultaneously.
