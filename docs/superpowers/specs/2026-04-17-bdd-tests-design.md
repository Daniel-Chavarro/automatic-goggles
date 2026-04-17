# BDD Tests Design

**Date:** 2026-04-17  
**Status:** Approved  
**Scope:** Mappers, Services, Repositories, Controllers, Config (integration)

## 1. Overview

Implement BDD-style unit and integration tests for all layers using JUnit 5 with Given/When/Then nested class structure. Tests follow test-first approach for business logic (services, mappers) and test-after for existing code.

## 2. Test Framework

- **JUnit 5** with nested classes
- **Mockito** for mocking (`@ExtendWith(MockitoExtension.class)`)
- **Spring Boot Test** for integration (`@DataJpaTest`, `@WebMvcTest`, `@SpringBootTest`)
- **H2 in-memory database** for repository tests

## 3. Directory Structure

```
src/test/java/org/java_avanzado/taller/
├── service/
│   ├── ProductServiceTest.java
│   ├── UserServiceTest.java
│   ├── OrderServiceTest.java
│   └── JwtServiceTest.java
├── utils/mapper/
│   ├── ProductMapperTest.java
│   ├── UserMapperTest.java
│   └── OrderMapperTest.java
├── persistence/repository/
│   ├── ProductRepositoryTest.java
│   ├── UserRepositoryTest.java
│   └── OrderRepositoryTest.java
├── controller/
│   ├── ProductControllerTest.java
│   ├── UserControllerTest.java
│   └── OrderControllerTest.java
└── config/
    └── SecurityConfigIntegrationTest.java (via controller integration)
```

## 4. Mapper Tests

- **MapStruct** generates implementations at compile time
- Use real mapper instances via `Mappers.getMapper(ProductMapper.class)`
- Test all mapping permutations:
  - Entity ↔ Domain
  - CreateRequest → Domain
  - UpdateRequest → Domain (with existing)
  - Domain → Response
  - List variants

**Pattern:**
```java
@Nested class given_* {
    @Nested class when_* {
        @BeforeEach void whenCall() { }
        @Nested class then_* {
            @Test void assertion() { }
        }
    }
}
```

## 5. Service Tests

- Use `@MockitoExtension` for auto-injection
- `@Mock` for repository dependencies
- `@InjectMocks` for service under test
- Test success and error paths for each method
- Verify repository interactions with `verify(mock).methodCall()`

**Scope:**
| Service | Methods to Test |
|---------|--------------|
| ProductService | getActiveProduct, createProduct, updateProduct, deleteProduct, getAllActiveProducts |
| UserService | registerUser, authenticate, getUserById, getAllUsers, disableUser, createUser, updateUser |
| OrderService | createOrder, getOrdersByUser, getAllOrders |
| JwtService | extractUsername, generateToken, isTokenValid |

## 6. Repository Tests

- Use `@DataJpaTest` with embedded H2
- Use `TestEntityManager` for setup
- Test query methods directly

**Scope:**
| Repository | Methods to Test |
|-----------|---------------|
| ProductRepository | findAllByName, findByIdAndActiveTrue |
| UserRepository | findAllByFirstNameLikeIgnoreCase, findAllByLastNameLikeIgnoreCase, findByIdAndActive, findByEmail |
| OrderRepository | findAllByUserId |

## 7. Controller Tests

- Use `@WebMvcTest` for slice testing
- Mock service/mapper dependencies with `@MockBean`
- Use `MockMvc` for HTTP assertions
- Test HTTP status, response body, headers

**Scope:**
| Controller | Endpoints to Test |
|-----------|-----------------|
| ProductController | GET /api/products, GET /api/products/{id}, POST /api/products, PUT /api/products/{id}, DELETE /api/products/{id} |
| UserController | GET /api/users, GET /api/users/{id}, POST /api/users, PUT /api/users/{id}, DELETE /api/users/{id} |
| OrderController | POST /api/orders/user/{userId}, GET /api/orders/user/{userId}, GET /api/orders |

## 8. Config Tests

- Test SecurityConfig via integration via protected endpoints
- Test authentication flow through `/api/auth/**` endpoints
- No separate unit tests for security filter

**Scope:**
- Unauthenticated → 401
- Invalid token → 401
- Valid token → 200/403 based on role

## 9. Test Data Patterns

- Use `@BeforeEach` to setup test data per nested class
- Use builder pattern for entities
- Use constants for repeated values
- Each test method tests one assertion

## 10. Execution

Run all tests with:
```bash
./mvnw test
```

## 11. Exclusions

- No Cucumber/Gherkin (plain JUnit 5 BDD naming only)
- No standalone SecurityConfig unit tests (integration only)
- No database container tests (H2 in-memory only)