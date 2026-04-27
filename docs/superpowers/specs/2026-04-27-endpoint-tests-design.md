# Test Design: REST Endpoints and Global Exception Handling

## 1. Architecture & Technologies
*   **Testing Frameworks:** JUnit 5, MockMvc, `@WebMvcTest`, `@SpringBootTest`, Hamcrest matchers.
*   **Authentication Strategy:** Hybrid approach. We will use `@WithMockUser` for isolated `@WebMvcTest` unit tests to simplify the authentication context. For Integration and E2E tests, we will generate and use actual JWT tokens in the headers to mimic real-world scenarios.
*   **Database:** We will use an embedded H2 Database for Integration and E2E tests to ensure fast execution and easy setup, while still testing the persistence layer integrations.

## 2. Unit Tests (`@WebMvcTest`)
*   **Scope:** Isolate each Controller (`AuthController`, `UserController`, `ProductController`, `OrderController`, `AuditController`).
*   **Approach:** Fully mock service dependencies (`@MockBean`). Focus on testing request mapping, input validation (Bean Validation annotations), DTO serialization/deserialization, and correct HTTP status codes for successful paths.
*   **Global Exception Handler:** We will use `@WebMvcTest` with mocked controllers throwing specific domain exceptions. This will verify that the `GlobalExceptionHandler` intercepts exceptions and returns the correct error JSON structure and HTTP status codes (e.g., 400 for `InsufficientStockException`, 404 for `UserNotFoundException`, 409 for `ProductAlreadyInOrderException`).

## 3. Integration Tests (`@SpringBootTest`)
*   **Scope:** Test individual endpoints with actual data flows (e.g., creating a product and ensuring it persists and can be retrieved) within the full Spring context.
*   **Validation:** Verify that pagination, sorting, and filtering work as expected on the repositories. Validate HTTP status codes returned by the endpoints. Ensure JWT security filter correctly validates tokens.

## 4. End-to-End Tests (`@SpringBootTest` with `@AutoConfigureMockMvc`)
*   **Scope:** Simulate complete user journeys across multiple controllers.
*   **Flows to Test:**
    *   **User flow:** Registration -> Login (receive JWT) -> List Products -> Create an Order -> Add Product to Order.
    *   **Admin flow:** Login -> Create User -> Create Product -> View Audit logs.
*   **Security Validation:** Verify strict role-based access control. Ensure that a `CLIENT` cannot access `ADMIN` endpoints (e.g., `POST /api/products`, `GET /api/users`), and that unauthenticated requests return 401 Unauthorized across protected resources.
