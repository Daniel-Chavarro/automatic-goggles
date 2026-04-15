# E-Commerce Backend Design Specification

## Overview
This document specifies the design for a backend REST API handling users, products, inventory, and purchase orders. It emphasizes a clear, sustainable software structure (Persistence, Domain, Service, Controller, Config, Utils) and ensures robust concurrency handling.

## Architectural Layers

### 1. Domain Layer (`domain`)
Encapsulates all core business rules independently of the database or web layers.
*   **Models**: `User`, `Product`, `Order`, `OrderProduct`.
    *   *Design Decision (Audit/Version Fields)*: The domain models should contain the `version` attribute (as it's critical for the optimistic locking business rule), but they **do not** need the `AuditableEntity` attributes (`createdAt`, `updatedAt`, etc.). Those are infrastructure concerns and belong strictly in the Persistence entities (and DTOs if the client needs to see them). This keeps the Domain pure.
*   **Exceptions**: `domain/exception/` containing `InsufficientStockException`, `ResourceNotFoundException`, `OptimisticLockingFailureException` (or a custom wrapper), and `UnauthorizedActionException`.

### 2. Persistence Layer (`persistence`)
*   **Entities**: `UserEntity`, `ProductEntity` (utilizing `active` for soft deletes and `@Version` for concurrency), `OrderEntity`, `OrderProductEntity`.
*   **Audit**: `EventLogEntity` specifically to track login attempts (success/fail) and unauthorized access attempts. This complements the existing `AuditableEntity` which timestamps generic entity creation/updates.
*   **Repositories**: Spring Data JPA interfaces with custom queries (e.g., filtering by `active = true`).

### 3. Service Layer (`service`)
Orchestrates domain logic and persistence.
*   **UserService**: Registration and JWT authentication.
*   **ProductService**: CRUD operations and soft-delete logic (`active = false`).
*   **OrderService**: Transactional boundary. Verifies stock, triggers domain `deductStock()` method, and saves the order.
*   **EventLogService**: Asynchronous (`@Async`) logging of security events (login attempts, unauthorized access).

### 4. Controller Layer (`controller`)
*   **Endpoints**: `AuthController`, `ProductController`, `OrderController`, `AuditController`.
*   **DTOs**: Request and Response objects.
*   **GlobalExceptionHandler**: A `@RestControllerAdvice` in the controller package to translate domain/Spring exceptions into clean HTTP JSON responses (400, 401, 403, 404, 409).

### 5. Config Layer (`config`)
*   **SecurityConfig**: Stateless session creation, JWT filter registration, and role-based access control (Admin vs. Client).
*   **JwtAuthenticationFilter**: Intercepts requests to validate JWTs.
*   **Database**: `docker-compose.yml` for PostgreSQL.

### 6. Utils Layer (`utils`)
*   **Mappers**: MapStruct interfaces (`UserMapper`, `ProductMapper`, `OrderMapper`) to convert between Entities, Models, and DTOs.

## Central Business Rule
**Concurrent Purchases & Stock Management:**
Multiple customers attempting to purchase the same product simultaneously are handled via **Optimistic Locking**. The `ProductEntity` uses an `@Version` field. If two transactions read the same stock and attempt to deduct it, the first to commit succeeds (incrementing the version). The second transaction fails with an `OptimisticLockingFailureException`, which the `GlobalExceptionHandler` catches and translates into a user-friendly 409 Conflict response.
