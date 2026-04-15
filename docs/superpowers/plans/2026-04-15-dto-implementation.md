# DTO Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add request and response DTOs to all controllers (User, Product, Order) to hide internal fields and add validation constraints.

**Architecture:** Create separate request/response DTOs in `controller/dto/request/` and `controller/dto/response/` packages following the approved design spec.

**Tech Stack:** Java 17, Spring Boot, Lombok

---

## Task 1: Create Product Request DTOs

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/request/create/CreateProductRequest.java`
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/request/update/UpdateProductRequest.java`

- [ ] **Step 1: Create CreateProductRequest.java**

```java
package org.java_avanzado.taller.controller.dto.request.create;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}
```

- [ ] **Step 2: Create UpdateProductRequest.java**

```java
package org.java_avanzado.taller.controller.dto.request.update;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {
    
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/dto/request/create/CreateProductRequest.java src/main/java/org/java_avanzado/taller/controller/dto/request/update/UpdateProductRequest.java
git commit -m "feat: add Product request DTOs (CreateProductRequest, UpdateProductRequest)"
```

---

## Task 2: Create Order Request DTOs

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/request/create/AddOrderItemRequest.java`
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/request/create/CreateOrderRequest.java`
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/request/update/UpdateOrderRequest.java`

- [ ] **Step 1: Create AddOrderItemRequest.java**

```java
package org.java_avanzado.taller.controller.dto.request.create;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddOrderItemRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
```

- [ ] **Step 2: Create CreateOrderRequest.java**

```java
package org.java_avanzado.taller.controller.dto.request.create;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<AddOrderItemRequest> items;
}
```

- [ ] **Step 3: Create UpdateOrderRequest.java**

```java
package org.java_avanzado.taller.controller.dto.request.update;

import lombok.Data;
import org.java_avanzado.taller.domain.model.OrderStatus;

@Data
public class UpdateOrderRequest {
    private OrderStatus status;
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/dto/request/create/AddOrderItemRequest.java src/main/java/org/java_avanzado/taller/controller/dto/request/create/CreateOrderRequest.java src/main/java/org/java_avanzado/taller/controller/dto/request/update/UpdateOrderRequest.java
git commit -m "feat: add Order request DTOs (AddOrderItemRequest, CreateOrderRequest, UpdateOrderRequest)"
```

---

## Task 3: Create User Request DTOs

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/request/update/UpdateUserRequest.java`

- [ ] **Step 1: Create UpdateUserRequest.java**

```java
package org.java_avanzado.taller.controller.dto.request.update;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    
    @Size(max = 100, message = "First name cannot be more than 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Last name cannot be more than 100 characters")
    private String lastName;
    
    @Pattern(regexp = "(^$|[0-9]{10})", message = "Phone must be a 10-digit number")
    private String phone;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/dto/request/update/UpdateUserRequest.java
git commit -m "feat: add UpdateUserRequest DTO"
```

---

## Task 4: Create Response DTOs Package and User Response DTOs

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/response/UserResponse.java`
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/response/UserSummaryResponse.java`

- [ ] **Step 1: Create response directory structure**

```bash
mkdir -p src/main/java/org/java_avanzado/taller/controller/dto/response
```

- [ ] **Step 2: Create UserResponse.java**

```java
package org.java_avanzado.taller.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: Create UserSummaryResponse.java**

```java
package org.java_avanzado.taller.controller.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserSummaryResponse {
    
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/dto/response/UserResponse.java src/main/java/org/java_avanzado/taller/controller/dto/response/UserSummaryResponse.java
git commit -m "feat: add User response DTOs (UserResponse, UserSummaryResponse)"
```

---

## Task 5: Create Product Response DTOs

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/response/ProductResponse.java`
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/response/ProductSummaryResponse.java`

- [ ] **Step 1: Create ProductResponse.java**

```java
package org.java_avanzado.taller.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private boolean active;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: Create ProductSummaryResponse.java**

```java
package org.java_avanzado.taller.controller.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductSummaryResponse {
    
    private Long id;
    private String name;
    private BigDecimal price;
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/dto/response/ProductResponse.java src/main/java/org/java_avanzado/taller/controller/dto/response/ProductSummaryResponse.java
git commit -m "feat: add Product response DTOs (ProductResponse, ProductSummaryResponse)"
```

---

## Task 6: Create Order Response DTOs

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/response/OrderItemResponse.java`
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/response/OrderResponse.java`
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/response/OrderSummaryResponse.java`

- [ ] **Step 1: Create OrderItemResponse.java**

```java
package org.java_avanzado.taller.controller.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}
```

- [ ] **Step 2: Create OrderResponse.java**

```java
package org.java_avanzado.taller.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    
    private Long id;
    private UUID userId;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private boolean active;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: Create OrderSummaryResponse.java**

```java
package org.java_avanzado.taller.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderSummaryResponse {
    
    private Long id;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/dto/response/OrderItemResponse.java src/main/java/org/java_avanzado/taller/controller/dto/response/OrderResponse.java src/main/java/org/java_avanzado/taller/controller/dto/response/OrderSummaryResponse.java
git commit -m "feat: add Order response DTOs (OrderItemResponse, OrderResponse, OrderSummaryResponse)"
```

---

## Task 7: Create Mappers

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/utils/mapper/UserResponseMapper.java`
- Create: `src/main/java/org/java_avanzado/taller/utils/mapper/ProductResponseMapper.java`
- Create: `src/main/java/org/java_avanzado/taller/utils/mapper/OrderResponseMapper.java`

- [ ] **Step 1: Create UserResponseMapper.java**

```java
package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.controller.dto.response.UserResponse;
import org.java_avanzado.taller.controller.dto.response.UserSummaryResponse;
import org.java_avanzado.taller.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserResponseMapper {
    
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }
    
    public UserSummaryResponse toSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }
    
    public List<UserResponse> toResponseList(List<User> users) {
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<UserSummaryResponse> toSummaryList(List<User> users) {
        return users.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 2: Create ProductResponseMapper.java**

```java
package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.controller.dto.response.ProductResponse;
import org.java_avanzado.taller.controller.dto.response.ProductSummaryResponse;
import org.java_avanzado.taller.domain.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductResponseMapper {
    
    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getQuantity())
                .active(product.isActive())
                .build();
    }
    
    public ProductSummaryResponse toSummary(Product product) {
        return ProductSummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .build();
    }
    
    public List<ProductResponse> toResponseList(List<Product> products) {
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<ProductSummaryResponse> toSummaryList(List<Product> products) {
        return products.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 3: Create OrderResponseMapper.java**

```java
package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.controller.dto.response.OrderItemResponse;
import org.java_avanzado.taller.controller.dto.response.OrderResponse;
import org.java_avanzado.taller.controller.dto.response.OrderSummaryResponse;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.service.ProductService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderResponseMapper {
    
    private final ProductService productService;
    
    public OrderResponseMapper(ProductService productService) {
        this.productService = productService;
    }
    
    public OrderItemResponse itemToResponse(OrderProduct item) {
        String productName = productService.getActiveProduct(item.getProductId()).getName();
        return OrderItemResponse.builder()
                .productId(item.getProductId())
                .productName(productName)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
    }
    
    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalPrice(order.getTotalPrice())
                .status(order.getOrderStatus())
                .items(order.getOrderProducts().stream()
                        .map(this::itemToResponse)
                        .collect(Collectors.toList()))
                .active(order.isActive())
                .createdAt(order.getCreatedAt())
                .build();
    }
    
    public OrderSummaryResponse toSummary(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
    
    public List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<OrderSummaryResponse> toSummaryList(List<Order> orders) {
        return orders.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/utils/mapper/UserResponseMapper.java src/main/java/org/java_avanzado/taller/utils/mapper/ProductResponseMapper.java src/main/java/org/java_avanzado/taller/utils/mapper/OrderResponseMapper.java
git commit -m "feat: add response mappers (UserResponseMapper, ProductResponseMapper, OrderResponseMapper)"
```

---

## Task 8: Update UserController

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/controller/UserController.java`

- [ ] **Step 1: Update UserController to use DTOs**

```java
package org.java_avanzado.taller.controller;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.CreateUserRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateUserRequest;
import org.java_avanzado.taller.controller.dto.response.UserResponse;
import org.java_avanzado.taller.controller.dto.response.UserSummaryResponse;
import org.java_avanzado.taller.service.UserService;
import org.java_avanzado.taller.utils.mapper.UserResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserResponseMapper userResponseMapper;

    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        return ResponseEntity.ok(userResponseMapper.toSummaryList(userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userResponseMapper.toResponse(userService.getUserById(id)));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userResponseMapper.toResponse(userService.createUser(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userResponseMapper.toResponse(userService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableUser(@PathVariable UUID id) {
        userService.disableUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/UserController.java
git commit -m "refactor: update UserController to use DTOs"
```

---

## Task 9: Update ProductController

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/controller/ProductController.java`

- [ ] **Step 1: Update ProductController to use DTOs**

```java
package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateProductRequest;
import org.java_avanzado.taller.controller.dto.response.ProductResponse;
import org.java_avanzado.taller.controller.dto.response.ProductSummaryResponse;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.utils.mapper.ProductResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductResponseMapper productResponseMapper;

    @GetMapping
    public ResponseEntity<List<ProductSummaryResponse>> getAllProducts() {
        return ResponseEntity.ok(productResponseMapper.toSummaryList(productService.getAllActiveProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productResponseMapper.toResponse(productService.getActiveProduct(id)));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productResponseMapper.toResponse(productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productResponseMapper.toResponse(productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/ProductController.java
git commit -m "refactor: update ProductController to use DTOs"
```

---

## Task 10: Update OrderController

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/controller/OrderController.java`

- [ ] **Step 1: Update OrderController to use DTOs**

```java
package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.CreateOrderRequest;
import org.java_avanzado.taller.controller.dto.response.OrderResponse;
import org.java_avanzado.taller.controller.dto.response.OrderSummaryResponse;
import org.java_avanzado.taller.service.OrderService;
import org.java_avanzado.taller.utils.mapper.OrderResponseMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderResponseMapper orderResponseMapper;

    @PostMapping("/user/{userId}")
    public ResponseEntity<OrderResponse> createOrder(@PathVariable UUID userId, @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponseMapper.toResponse(orderService.createOrder(userId, request)));
    }

    @Transactional(readOnly = true)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderSummaryResponse>> getUserOrders(@PathVariable UUID userId) {
        return ResponseEntity.ok(orderResponseMapper.toSummaryList(orderService.getOrdersByUser(userId)));
    }

    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getAllOrders() {
        return ResponseEntity.ok(orderResponseMapper.toSummaryList(orderService.getAllOrders()));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/OrderController.java
git commit -m "refactor: update OrderController to use DTOs"
```

---

## Task 11: Update Services to Support DTOs

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/service/UserService.java`
- Modify: `src/main/java/org/java_avanzado/taller/service/ProductService.java`
- Modify: `src/main/java/org/java_avanzado/taller/service/OrderService.java`

- [ ] **Step 1: Check UserService for createUser and updateUser methods**

Read UserService to see if it needs updates to accept request DTOs.

- [ ] **Step 2: Check ProductService for createProduct and updateProduct methods**

Read ProductService to see if it needs updates to accept request DTOs.

- [ ] **Step 3: Check OrderService for createOrder method**

Read OrderService to see if it needs updates to accept CreateOrderRequest.

- [ ] **Step 4: Commit any service changes**

---

## Task 12: Run Tests and Verify

- [ ] **Step 1: Run the application**

```bash
./mvnw spring-boot:run
```

- [ ] **Step 2: Run tests**

```bash
./mvnw test
```

- [ ] **Step 3: Verify API responses**

Test each endpoint to ensure DTOs are returned correctly and internal fields are hidden.

- [ ] **Step 4: Final commit**

```bash
git add .
git commit -m "test: run tests and verify DTO implementation"
```