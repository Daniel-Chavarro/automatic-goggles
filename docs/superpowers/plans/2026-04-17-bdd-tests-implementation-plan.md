# BDD Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build behavior-style (Given/When/Then) automated tests for mappers, services, repositories, controllers, and selected security configuration behavior.

**Architecture:** Use layered test slices to keep feedback fast and isolated: pure unit tests for mappers/services, `@DataJpaTest` for repositories, `@WebMvcTest` for controllers, and one focused `@SpringBootTest` for security integration behavior. Keep BDD structure with `@Nested` classes (`given_*`, `when_*`, `then_*`) and clear single-behavior assertions.

**Tech Stack:** Java 21, JUnit 5, Mockito, Spring Boot Test (`@DataJpaTest`, `@WebMvcTest`, `@SpringBootTest`), MockMvc, H2, MapStruct.

---

## File Structure Map

- `src/test/java/org/java_avanzado/taller/support/TestDataFactory.java`: shared builders for domain/entity/request test objects.
- `src/test/java/org/java_avanzado/taller/utils/mapper/*.java`: mapper behavior tests for Product/User/Order map directions.
- `src/test/java/org/java_avanzado/taller/service/*.java`: unit tests for business logic, validation, repository interactions.
- `src/test/java/org/java_avanzado/taller/persistence/repository/*.java`: repository query behavior against H2.
- `src/test/java/org/java_avanzado/taller/controller/*.java`: endpoint behavior tests for request/response/status mapping.
- `src/test/java/org/java_avanzado/taller/config/SecurityConfigIntegrationTest.java`: auth/permit/deny behavior through real security filter chain.

---

### Task 1: Add Shared Test Data Factory

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/support/TestDataFactory.java`

- [ ] **Step 1: Write the failing test usage expectation**

Create one mapper test first that references `TestDataFactory.productEntity()` so compile fails before factory exists.

```java
// In ProductMapperTest.java (temporary first line in one test)
var entity = TestDataFactory.productEntity();
```

- [ ] **Step 2: Run compile to confirm failure**

Run: `./mvnw -q -Dtest=ProductMapperTest test`

Expected: FAIL with compile error similar to `cannot find symbol: class TestDataFactory`.

- [ ] **Step 3: Create minimal factory implementation**

```java
package org.java_avanzado.taller.support;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.java_avanzado.taller.controller.dto.request.create.AddOrderItemRequest;
import org.java_avanzado.taller.controller.dto.request.create.CreateOrderRequest;
import org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest;
import org.java_avanzado.taller.controller.dto.request.create.CreateUserRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateProductRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateUserRequest;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.domain.model.OrderStatus;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.domain.model.UserRole;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.OrderProductEntity;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;

public final class TestDataFactory {
    private TestDataFactory() {}

    public static Product product() {
        return Product.builder()
                .id(1L)
                .name("Coffee")
                .description("Ground coffee")
                .price(new BigDecimal("12.50"))
                .quantity(20)
                .active(true)
                .build();
    }

    public static ProductEntity productEntity() {
        return ProductEntity.builder()
                .id(1L)
                .name("Coffee")
                .description("Ground coffee")
                .price(new BigDecimal("12.50"))
                .stockQuantity(20)
                .active(true)
                .build();
    }

    public static CreateProductRequest createProductRequest() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Coffee");
        request.setDescription("Ground coffee");
        request.setPrice(new BigDecimal("12.50"));
        request.setQuantity(20);
        return request;
    }

    public static UpdateProductRequest updateProductRequest() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Coffee Premium");
        request.setDescription("Premium ground coffee");
        request.setPrice(new BigDecimal("16.00"));
        request.setQuantity(10);
        return request;
    }

    public static User user() {
        return User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .firstName("Ana")
                .lastName("Lopez")
                .email("ana@example.com")
                .phone("3001112233")
                .password("StrongPass1")
                .role(UserRole.CLIENT)
                .active(true)
                .build();
    }

    public static UserEntity userEntity() {
        return UserEntity.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .firstName("Ana")
                .lastName("Lopez")
                .email("ana@example.com")
                .phone("3001112233")
                .password("$2a$10$hash")
                .role(UserRole.CLIENT)
                .active(true)
                .build();
    }

    public static CreateUserRequest createUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Ana");
        request.setLastName("Lopez");
        request.setEmail("ana@example.com");
        request.setPhone("3001112233");
        request.setPassword("StrongPass1");
        request.setRole(UserRole.CLIENT);
        return request;
    }

    public static UpdateUserRequest updateUserRequest() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Ana Maria");
        request.setLastName("Lopez Ruiz");
        request.setPhone("3004445566");
        return request;
    }

    public static Order order() {
        return Order.builder()
                .id(9L)
                .userId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .totalPrice(new BigDecimal("25.00"))
                .orderStatus(OrderStatus.APPROVED)
                .orderProducts(List.of(orderProduct()))
                .active(true)
                .build();
    }

    public static OrderProduct orderProduct() {
        return OrderProduct.builder()
                .id(2L)
                .productId("1")
                .quantity(2)
                .unitPrice(new BigDecimal("12.50"))
                .build();
    }

    public static OrderEntity orderEntity() {
        UserEntity user = userEntity();
        OrderEntity order = OrderEntity.builder()
                .id(9L)
                .user(user)
                .totalPrice(new BigDecimal("25.00"))
                .orderStatus(OrderStatus.APPROVED)
                .active(true)
                .build();
        OrderProductEntity item = OrderProductEntity.builder()
                .id(2L)
                .order(order)
                .product(productEntity())
                .quantity(2)
                .unitPrice(new BigDecimal("12.50"))
                .build();
        order.setOrderProducts(List.of(item));
        return order;
    }

    public static CreateOrderRequest createOrderRequest() {
        AddOrderItemRequest item = new AddOrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(item));
        return request;
    }
}
```

- [ ] **Step 4: Run targeted test to verify pass**

Run: `./mvnw -q -Dtest=ProductMapperTest test`

Expected: PASS (or no compile error if mapper test not created yet).

- [ ] **Step 5: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/support/TestDataFactory.java
git commit -m "test: add shared test data factory for behavior tests"
```

---

### Task 2: Implement Mapper Behavior Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/utils/mapper/ProductMapperTest.java`
- Create: `src/test/java/org/java_avanzado/taller/utils/mapper/UserMapperTest.java`
- Create: `src/test/java/org/java_avanzado/taller/utils/mapper/OrderMapperTest.java`

- [ ] **Step 1: Write ProductMapper failing tests**

```java
package org.java_avanzado.taller.utils.mapper;

import static org.junit.jupiter.api.Assertions.*;
import org.java_avanzado.taller.support.TestDataFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductMapperTest {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Nested
    class given_productEntity {
        @Test
        void when_mappingToDomain_then_mapsStockQuantityToQuantity() {
            var mapped = mapper.fromProductEntityToDomain(TestDataFactory.productEntity());
            assertEquals(20, mapped.getQuantity());
            assertEquals("Coffee", mapped.getName());
        }
    }

    @Nested
    class given_updateProductRequest {
        @Test
        void when_mappingWithExisting_then_preservesIgnoredFields() {
            var existing = TestDataFactory.product();
            existing.setId(999L);
            var mapped = mapper.fromUpdateProductRequestToDomain(TestDataFactory.updateProductRequest(), existing);
            assertEquals(999L, mapped.getId());
            assertEquals("Coffee Premium", mapped.getName());
        }
    }
}
```

- [ ] **Step 2: Add UserMapper and OrderMapper behavior tests**

```java
// UserMapperTest.java
package org.java_avanzado.taller.utils.mapper;

import static org.junit.jupiter.api.Assertions.*;
import org.java_avanzado.taller.support.TestDataFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserMapperTest {
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Nested
    class given_userEntity {
        @Test
        void when_mappingToDomain_then_mapsCoreFields() {
            var mapped = mapper.fromUserEntityToDomain(TestDataFactory.userEntity());
            assertEquals("ana@example.com", mapped.getEmail());
            assertEquals("Ana", mapped.getFirstName());
        }
    }

    @Nested
    class given_updateUserRequest {
        @Test
        void when_mappingWithExisting_then_keepsEmailRoleAndPassword() {
            var existing = TestDataFactory.user();
            var mapped = mapper.fromUpdateUserRequestToDomain(TestDataFactory.updateUserRequest(), existing);
            assertEquals(existing.getEmail(), mapped.getEmail());
            assertEquals(existing.getRole(), mapped.getRole());
            assertEquals(existing.getPassword(), mapped.getPassword());
        }
    }
}
```

```java
// OrderMapperTest.java
package org.java_avanzado.taller.utils.mapper;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.java_avanzado.taller.support.TestDataFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class OrderMapperTest {
    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Nested
    class given_orderEntity {
        @Test
        void when_mappingToDomain_then_mapsOrderStatusAndItems() {
            var mapped = mapper.fromOrderEntityToDomain(TestDataFactory.orderEntity());
            assertEquals(1, mapped.getOrderProducts().size());
            assertEquals("1", mapped.getOrderProducts().get(0).getProductId());
        }
    }

    @Nested
    class given_orderAndProductNameContext {
        @Test
        void when_mappingToResponse_then_resolvesProductNames() {
            var response = mapper.fromOrderToResponse(TestDataFactory.order(), Map.of("1", "Coffee"));
            assertEquals("Coffee", response.getItems().get(0).getProductName());
            assertEquals(1L, response.getItems().get(0).getProductId());
        }
    }
}
```

- [ ] **Step 3: Run mapper tests**

Run: `./mvnw -q -Dtest=ProductMapperTest,UserMapperTest,OrderMapperTest test`

Expected: PASS with 3 test classes green.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/utils/mapper/ProductMapperTest.java src/test/java/org/java_avanzado/taller/utils/mapper/UserMapperTest.java src/test/java/org/java_avanzado/taller/utils/mapper/OrderMapperTest.java
git commit -m "test: add behavior tests for mapstruct mappers"
```

---

### Task 3: Implement ProductService and UserService Behavior Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/service/ProductServiceTest.java`
- Create: `src/test/java/org/java_avanzado/taller/service/UserServiceTest.java`

- [ ] **Step 1: Write ProductService tests first (failing)**

```java
package org.java_avanzado.taller.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.java_avanzado.taller.domain.exception.ProductNotFoundException;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.utils.mapper.ProductMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock ProductRepository productRepository;
    @Mock ProductMapper productMapper;
    @InjectMocks ProductService productService;

    @Nested
    class given_existingActiveProduct {
        @Test
        void when_getActiveProduct_then_returnsMappedDomain() {
            when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(TestDataFactory.productEntity()));
            when(productMapper.fromProductEntityToDomain(any())).thenReturn(TestDataFactory.product());

            var result = productService.getActiveProduct(1L);

            assertEquals("Coffee", result.getName());
            verify(productRepository).findByIdAndActiveTrue(1L);
        }
    }

    @Nested
    class given_missingProduct {
        @Test
        void when_getActiveProduct_then_throwsProductNotFoundException() {
            when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());
            assertThrows(ProductNotFoundException.class, () -> productService.getActiveProduct(1L));
        }
    }

    @Nested
    class given_mixedActiveAndInactiveProducts {
        @Test
        void when_getAllActiveProducts_then_returnsOnlyActiveOnes() {
            var active = TestDataFactory.productEntity();
            var inactive = TestDataFactory.productEntity();
            inactive.setId(2L);
            inactive.setActive(false);
            when(productRepository.findAll()).thenReturn(List.of(active, inactive));
            when(productMapper.fromProductEntityToDomain(active)).thenReturn(TestDataFactory.product());

            var result = productService.getAllActiveProducts();
            assertEquals(1, result.size());
        }
    }
}
```

- [ ] **Step 2: Write UserService tests (failing then passing)**

```java
package org.java_avanzado.taller.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.java_avanzado.taller.domain.exception.EmailAlreadyExistsException;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.utils.mapper.UserMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @InjectMocks UserService userService;

    @Nested
    class given_newUserEmail {
        @Test
        void when_registerUser_then_savesEncodedPasswordAndReturnsToken() {
            var user = TestDataFactory.user();
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
            when(userMapper.fromUserToEntity(user)).thenReturn(TestDataFactory.userEntity());
            when(passwordEncoder.encode(user.getPassword())).thenReturn("encoded");
            when(jwtService.generateToken(user.getEmail())).thenReturn("jwt-token");

            var token = userService.registerUser(user);

            assertEquals("jwt-token", token);
            verify(userRepository).save(any());
        }
    }

    @Nested
    class given_duplicateEmail {
        @Test
        void when_registerUser_then_throwsEmailAlreadyExistsException() {
            var user = TestDataFactory.user();
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(TestDataFactory.userEntity()));
            assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(user));
        }
    }

    @Nested
    class given_validCredentials {
        @Test
        void when_authenticate_then_returnsToken() {
            var entity = TestDataFactory.userEntity();
            entity.setPassword("encoded");
            when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(entity));
            when(passwordEncoder.matches("StrongPass1", "encoded")).thenReturn(true);
            when(jwtService.generateToken("ana@example.com")).thenReturn("jwt-token");

            var token = userService.authenticate("ana@example.com", "StrongPass1");
            assertEquals("jwt-token", token);
        }
    }
}
```

- [ ] **Step 3: Run service tests**

Run: `./mvnw -q -Dtest=ProductServiceTest,UserServiceTest test`

Expected: PASS with service behavior scenarios green.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/service/ProductServiceTest.java src/test/java/org/java_avanzado/taller/service/UserServiceTest.java
git commit -m "test: add behavior tests for product and user services"
```

---

### Task 4: Implement OrderService and JwtService Behavior Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/service/OrderServiceTest.java`
- Create: `src/test/java/org/java_avanzado/taller/service/JwtServiceTest.java`

- [ ] **Step 1: Add OrderService behavior tests**

```java
package org.java_avanzado.taller.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.java_avanzado.taller.persistence.repository.OrderRepository;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.utils.mapper.OrderMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock OrderMapper orderMapper;
    @InjectMocks OrderService orderService;

    @Nested
    class given_availableStock {
        @Test
        void when_createOrder_then_updatesStockAndPersistsOrder() {
            var request = TestDataFactory.createOrderRequest();
            var productEntity = TestDataFactory.productEntity();
            var mappedOrder = TestDataFactory.orderEntity();
            var userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

            when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(productEntity));
            when(orderMapper.fromOrderToEntity(any())).thenReturn(mappedOrder);
            when(orderRepository.save(mappedOrder)).thenReturn(mappedOrder);
            when(orderMapper.fromOrderEntityToDomain(mappedOrder)).thenReturn(TestDataFactory.order());

            var result = orderService.createOrder(userId, request);

            assertEquals(new BigDecimal("25.00"), result.getTotalPrice());
            verify(productRepository, atLeastOnce()).save(any());
            verify(orderRepository).save(mappedOrder);
        }
    }

    @Nested
    class given_missingProduct {
        @Test
        void when_createOrder_then_throwsIllegalArgumentException() {
            when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () ->
                    orderService.createOrder(UUID.randomUUID(), TestDataFactory.createOrderRequest()));
        }
    }

    @Nested
    class given_existingOrdersForUser {
        @Test
        void when_getOrdersByUser_then_mapsAllOrders() {
            var userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            when(orderRepository.findAllByUserId(userId)).thenReturn(List.of(TestDataFactory.orderEntity()));
            when(orderMapper.fromOrderEntityToDomain(any())).thenReturn(TestDataFactory.order());
            assertEquals(1, orderService.getOrdersByUser(userId).size());
        }
    }
}
```

- [ ] **Step 2: Add JwtService behavior tests**

```java
package org.java_avanzado.taller.service;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setup() throws Exception {
        jwtService = new JwtService();
        Field secret = JwtService.class.getDeclaredField("secret");
        secret.setAccessible(true);
        secret.set(jwtService, "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        Field expiration = JwtService.class.getDeclaredField("expiration");
        expiration.setAccessible(true);
        expiration.set(jwtService, 3600000L);
    }

    @Nested
    class given_validEmail {
        @Test
        void when_generateToken_then_extractUsernameReturnsSameEmail() {
            String token = jwtService.generateToken("ana@example.com");
            assertEquals("ana@example.com", jwtService.extractUsername(token));
        }
    }

    @Nested
    class given_validTokenAndUser {
        @Test
        void when_isTokenValid_then_returnsTrue() {
            String token = jwtService.generateToken("ana@example.com");
            assertTrue(jwtService.isTokenValid(token, "ana@example.com"));
        }
    }

    @Nested
    class given_invalidToken {
        @Test
        void when_extractUsername_then_throwsNullPointerExceptionCurrentBehavior() {
            assertThrows(NullPointerException.class, () -> jwtService.extractUsername("broken.token.value"));
        }
    }
}
```

- [ ] **Step 3: Run tests for order and JWT services**

Run: `./mvnw -q -Dtest=OrderServiceTest,JwtServiceTest test`

Expected: PASS with all behavior scenarios green.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/service/OrderServiceTest.java src/test/java/org/java_avanzado/taller/service/JwtServiceTest.java
git commit -m "test: add behavior tests for order and jwt services"
```

---

### Task 5: Implement Repository Behavior Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/persistence/repository/ProductRepositoryTest.java`
- Create: `src/test/java/org/java_avanzado/taller/persistence/repository/UserRepositoryTest.java`
- Create: `src/test/java/org/java_avanzado/taller/persistence/repository/OrderRepositoryTest.java`

- [ ] **Step 1: Add ProductRepository behavior tests**

```java
package org.java_avanzado.taller.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ProductRepositoryTest {
    @Autowired ProductRepository repository;

    @Nested
    class given_activeAndInactiveProducts {
        @Test
        void when_findByIdAndActiveTrue_then_returnsOnlyActiveProduct() {
            ProductEntity active = repository.save(ProductEntity.builder().name("Coffee").description("d")
                    .price(java.math.BigDecimal.ONE).stockQuantity(4).active(true).build());
            repository.save(ProductEntity.builder().name("Coffee").description("d")
                    .price(java.math.BigDecimal.ONE).stockQuantity(4).active(false).build());

            Optional<ProductEntity> found = repository.findByIdAndActiveTrue(active.getId());

            assertTrue(found.isPresent());
            assertTrue(found.get().isActive());
        }
    }
}
```

- [ ] **Step 2: Add UserRepository and OrderRepository behavior tests**

```java
// UserRepositoryTest.java
package org.java_avanzado.taller.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.java_avanzado.taller.domain.model.UserRole;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserRepositoryTest {
    @Autowired UserRepository repository;

    @Nested
    class given_usersInDatabase {
        @Test
        void when_findByEmail_then_returnsMatchingUser() {
            repository.save(UserEntity.builder().firstName("Ana").lastName("Lopez")
                    .email("ana@example.com").password("x").role(UserRole.CLIENT).active(true).build());

            var found = repository.findByEmail("ana@example.com");
            assertTrue(found.isPresent());
            assertEquals("Ana", found.get().getFirstName());
        }
    }
}
```

```java
// OrderRepositoryTest.java
package org.java_avanzado.taller.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.java_avanzado.taller.domain.model.OrderStatus;
import org.java_avanzado.taller.domain.model.UserRole;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class OrderRepositoryTest {
    @Autowired OrderRepository orderRepository;
    @Autowired UserRepository userRepository;

    @Nested
    class given_ordersForDifferentUsers {
        @Test
        void when_findAllByUserId_then_returnsOnlyUsersOrders() {
            UserEntity user1 = userRepository.save(UserEntity.builder().firstName("Ana").lastName("Lopez")
                    .email("ana@example.com").password("x").role(UserRole.CLIENT).active(true).build());
            UserEntity user2 = userRepository.save(UserEntity.builder().firstName("Luis").lastName("Diaz")
                    .email("luis@example.com").password("x").role(UserRole.CLIENT).active(true).build());

            orderRepository.save(OrderEntity.builder().user(user1).totalPrice(new BigDecimal("15.00"))
                    .orderStatus(OrderStatus.APPROVED).active(true).build());
            orderRepository.save(OrderEntity.builder().user(user2).totalPrice(new BigDecimal("20.00"))
                    .orderStatus(OrderStatus.APPROVED).active(true).build());

            assertEquals(1, orderRepository.findAllByUserId(user1.getId()).size());
        }
    }
}
```

- [ ] **Step 3: Run repository tests**

Run: `./mvnw -q -Dtest=ProductRepositoryTest,UserRepositoryTest,OrderRepositoryTest test`

Expected: PASS with all JPA query behaviors verified.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/persistence/repository/ProductRepositoryTest.java src/test/java/org/java_avanzado/taller/persistence/repository/UserRepositoryTest.java src/test/java/org/java_avanzado/taller/persistence/repository/OrderRepositoryTest.java
git commit -m "test: add behavior tests for repositories with DataJpaTest"
```

---

### Task 6: Implement Product/User/Order Controller Behavior Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/controller/ProductControllerTest.java`
- Create: `src/test/java/org/java_avanzado/taller/controller/UserControllerTest.java`
- Create: `src/test/java/org/java_avanzado/taller/controller/OrderControllerTest.java`

- [ ] **Step 1: Add ProductController behavior tests**

```java
package org.java_avanzado.taller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.utils.mapper.ProductMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean ProductService productService;
    @MockBean ProductMapper productMapper;

    @Nested
    class given_productsAvailable {
        @Test
        void when_getAllProducts_then_returns200AndList() throws Exception {
            when(productService.getAllActiveProducts()).thenReturn(List.of(TestDataFactory.product()));
            when(productMapper.fromProductListToSummaryList(any())).thenReturn(List.of(
                    org.java_avanzado.taller.controller.dto.response.ProductSummaryResponse.builder()
                            .id(1L).name("Coffee").price(new java.math.BigDecimal("12.50")).build()));

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Coffee"));
        }
    }

    @Nested
    class given_validCreateProductPayload {
        @Test
        void when_createProduct_then_returns200AndResponseBody() throws Exception {
            when(productService.createProduct(any(org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest.class)))
                    .thenReturn(TestDataFactory.product());
            when(productMapper.fromProductToResponse(any())).thenReturn(
                    org.java_avanzado.taller.controller.dto.response.ProductResponse.builder()
                            .id(1L).name("Coffee").price(new java.math.BigDecimal("12.50")).build());

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Coffee\",\"price\":12.50,\"quantity\":5}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }
    }
}
```

- [ ] **Step 2: Add UserController and OrderController behavior tests**

```java
// UserControllerTest.java
package org.java_avanzado.taller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.java_avanzado.taller.service.UserService;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.utils.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean UserService userService;
    @MockBean UserMapper userMapper;

    @Test
    void given_users_when_getAllUsers_then_returns200AndSummaryList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(TestDataFactory.user()));
        when(userMapper.fromUserListToSummaryList(any())).thenReturn(List.of(
                org.java_avanzado.taller.controller.dto.response.UserSummaryResponse.builder()
                        .id(TestDataFactory.user().getId()).firstName("Ana").lastName("Lopez").email("ana@example.com").build()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ana@example.com"));
    }
}
```

```java
// OrderControllerTest.java
package org.java_avanzado.taller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Map;
import java.util.UUID;
import org.java_avanzado.taller.service.OrderService;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.utils.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean OrderService orderService;
    @MockBean ProductService productService;
    @MockBean OrderMapper orderMapper;

    @Test
    void given_validOrderRequest_when_createOrder_then_returns201() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(orderService.createOrder(any(UUID.class), any())).thenReturn(TestDataFactory.order());
        when(productService.getActiveProduct(1L)).thenReturn(TestDataFactory.product());
        when(orderMapper.fromOrderToResponse(any(), any(Map.class))).thenReturn(
                org.java_avanzado.taller.controller.dto.response.OrderResponse.builder()
                        .id(9L).status(org.java_avanzado.taller.domain.model.OrderStatus.APPROVED)
                        .totalPrice(new java.math.BigDecimal("25.00")).build());

        mockMvc.perform(post("/api/orders/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9));
    }
}
```

- [ ] **Step 3: Run controller tests**

Run: `./mvnw -q -Dtest=ProductControllerTest,UserControllerTest,OrderControllerTest test`

Expected: PASS with endpoint status/body behavior green.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/controller/ProductControllerTest.java src/test/java/org/java_avanzado/taller/controller/UserControllerTest.java src/test/java/org/java_avanzado/taller/controller/OrderControllerTest.java
git commit -m "test: add behavior tests for product user and order controllers"
```

---

### Task 7: Implement AuthController and AuditController Behavior Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/controller/AuthControllerTest.java`
- Create: `src/test/java/org/java_avanzado/taller/controller/AuditControllerTest.java`

- [ ] **Step 1: Add AuthController tests for register/login behavior**

```java
package org.java_avanzado.taller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.java_avanzado.taller.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean UserService userService;

    @Nested
    class given_validRegisterRequest {
        @Test
        void when_register_then_returnsToken() throws Exception {
            when(userService.registerUser(any())).thenReturn("jwt-token");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"ana@example.com\",\"password\":\"StrongPass1\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"));
        }
    }

    @Nested
    class given_missingCredentials {
        @Test
        void when_login_then_returns400() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"ana@example.com\"}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
```

- [ ] **Step 2: Add AuditController behavior tests**

```java
package org.java_avanzado.taller.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDateTime;
import java.util.List;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.java_avanzado.taller.service.EventLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean EventLogService eventLogService;

    @Test
    void given_logsAvailable_when_getAuditLogs_then_returns200() throws Exception {
        when(eventLogService.getAllLogs()).thenReturn(List.of(EventLogEntity.builder()
                .id(1L).eventType("LOGIN_SUCCESS").details("ok").username("ana@example.com")
                .timestamp(LocalDateTime.now()).build()));

        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 3: Run auth/audit controller tests**

Run: `./mvnw -q -Dtest=AuthControllerTest,AuditControllerTest test`

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/controller/AuthControllerTest.java src/test/java/org/java_avanzado/taller/controller/AuditControllerTest.java
git commit -m "test: add behavior tests for auth and audit controllers"
```

---

### Task 8: Implement Security Configuration Integration Behavior Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/config/SecurityConfigIntegrationTest.java`

- [ ] **Step 1: Write failing security integration scenarios**

```java
package org.java_avanzado.taller.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.Optional;
import org.java_avanzado.taller.domain.model.UserRole;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.service.JwtService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {
    @Autowired MockMvc mockMvc;

    @MockBean JwtService jwtService;
    @MockBean UserRepository userRepository;

    @MockBean org.java_avanzado.taller.service.ProductService productService;
    @MockBean org.java_avanzado.taller.utils.mapper.ProductMapper productMapper;

    @Nested
    class given_noToken {
        @Test
        void when_accessProtectedEndpoint_then_returns401() throws Exception {
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class given_invalidToken {
        @Test
        void when_accessProtectedEndpoint_then_returns401() throws Exception {
            when(jwtService.extractUsername("bad-token")).thenReturn(null);
            mockMvc.perform(get("/api/products").header("Authorization", "Bearer bad-token"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class given_validToken {
        @Test
        void when_accessProtectedEndpoint_then_returns200() throws Exception {
            when(jwtService.extractUsername("good-token")).thenReturn("ana@example.com");
            when(jwtService.isTokenValid("good-token", "ana@example.com")).thenReturn(true);
            when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(
                    UserEntity.builder().email("ana@example.com").password("x").firstName("Ana")
                            .lastName("Lopez").role(UserRole.CLIENT).active(true).build()));
            when(productService.getAllActiveProducts()).thenReturn(List.of());
            when(productMapper.fromProductListToSummaryList(List.of())).thenReturn(List.of());

            mockMvc.perform(get("/api/products").header("Authorization", "Bearer good-token"))
                    .andExpect(status().isOk());
        }
    }
}
```

- [ ] **Step 2: Run integration test class**

Run: `./mvnw -q -Dtest=SecurityConfigIntegrationTest test`

Expected: PASS verifying 401/200 behavior boundaries.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/config/SecurityConfigIntegrationTest.java
git commit -m "test: add security configuration integration behavior tests"
```

---

### Task 9: Stabilize BDD Naming and One-Behavior Assertions Across Suite

**Files:**
- Modify: `src/test/java/org/java_avanzado/taller/**/*.java` (all newly added tests)

- [ ] **Step 1: Refactor test method names to Given/When/Then style consistently**

Apply this naming style to each test method:

```java
// Before
void testGetAllUsers() { ... }

// After
void given_usersInSystem_when_getAllUsers_then_returnsSummaryList() { ... }
```

- [ ] **Step 2: Enforce one primary behavior assertion per test**

Split multi-assert tests into separate methods where needed:

```java
@Test
void given_validToken_when_accessProducts_then_returns200() { ... }

@Test
void given_validToken_when_accessProducts_then_returnsJsonArray() { ... }
```

- [ ] **Step 3: Run all newly added tests only**

Run: `./mvnw -q -Dtest='*MapperTest,*ServiceTest,*RepositoryTest,*ControllerTest,SecurityConfigIntegrationTest' test`

Expected: PASS for new test classes.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/java_avanzado/taller
git commit -m "test: standardize bdd naming and single-behavior assertions"
```

---

### Task 10: Full Verification and Documentation Update

**Files:**
- Modify: `README.md` (optional test command section if absent)

- [ ] **Step 1: Run full test suite**

Run: `./mvnw test`

Expected: BUILD SUCCESS.

- [ ] **Step 2: If full suite fails due unrelated pre-existing issues, capture and isolate**

Run: `./mvnw -q -Dtest='*MapperTest,*ServiceTest,*RepositoryTest,*ControllerTest,SecurityConfigIntegrationTest' test`

Expected: PASS for new BDD suite even if unrelated legacy tests fail.

- [ ] **Step 3: Add short README test section (only if missing)**

```markdown
## Testing

- Run all tests: `./mvnw test`
- Run BDD behavior suite only: `./mvnw -q -Dtest='*MapperTest,*ServiceTest,*RepositoryTest,*ControllerTest,SecurityConfigIntegrationTest' test`
```

- [ ] **Step 4: Commit final verification/docs**

```bash
git add README.md
git commit -m "docs: add commands for behavior-oriented test suite"
```

---

## Plan Self-Review

- **Spec coverage check:**
  - Mappers: covered in Task 2.
  - Services: covered in Tasks 3 and 4.
  - Repositories: covered in Task 5.
  - Controllers: covered in Tasks 6 and 7.
  - Config (integration): covered in Task 8.
  - BDD behavior style + naming: covered in Task 9.
- **Placeholder scan:** No `TODO`/`TBD` placeholders remain; every task includes concrete files, code, and commands.
- **Type consistency:** Names align with existing code (`ProductMapper`, `UserMapper`, `OrderMapper`, `JwtService`, `SecurityConfig`).
