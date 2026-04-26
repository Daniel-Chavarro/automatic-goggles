# Products, Orders, and Audit API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the remaining CRUD operations and REST API endpoints for Products, Orders (with transactional concurrency handling), and Audit Logging.

**Architecture:** Completing the standard Spring Boot layered architecture (Controller -> Service -> Persistence).

**Tech Stack:** Java 21, Spring Boot, Spring Security, MapStruct.

---

### Task 1: Fix Enum Compilation Errors

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/domain/model/UserRole.java`
- Modify: `src/main/java/org/java_avanzado/taller/domain/model/OrderStatus.java`

- [ ] **Step 1: Fix UserRole Enum**

```java
// In src/main/java/org/java_avanzado/taller/domain/model/UserRole.java
// Remove @RequiredArgsConstructor and explicitly define constructor
package org.java_avanzado.taller.domain.model;

import lombok.Getter;

@Getter
public enum UserRole {
    CLIENT("CLIENT"),
    ADMIN("ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }
}
```

- [ ] **Step 2: Fix OrderStatus Enum**

```java
// In src/main/java/org/java_avanzado/taller/domain/model/OrderStatus.java
// Remove @RequiredArgsConstructor and explicitly define constructor
package org.java_avanzado.taller.domain.model;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }
}
```

- [ ] **Step 3: Compile and Commit**

Run: `./mvnw compile`
Expected: Build SUCCESS

```bash
git add src/main/java/org/java_avanzado/taller/domain/model/UserRole.java src/main/java/org/java_avanzado/taller/domain/model/OrderStatus.java
git commit -m "fix(domain): add explicit constructors to enums to fix compiler errors"
```

### Task 2: Complete Product CRUD

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/service/ProductService.java`
- Create: `src/main/java/org/java_avanzado/taller/controller/ProductController.java`

- [ ] **Step 1: Add CRUD to ProductService**

```java
// In src/main/java/org/java_avanzado/taller/service/ProductService.java
// Add these methods inside the class

import org.java_avanzado.taller.exception.ProductNotFoundException;

@Transactional
public Product createProduct(Product product) {
    ProductEntity entity = productMapper.toEntity(product);
    entity.setActive(true);
    return productMapper.toDomain(productRepository.save(entity));
}

@Transactional
public Product updateProduct(Long id, Product product) {
    ProductEntity entity = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    entity.setName(product.getName());
    entity.setDescription(product.getDescription());
    entity.setPrice(product.getPrice());
    entity.setStockQuantity(product.getQuantity());
    return productMapper.toDomain(productRepository.save(entity));
}

@Transactional
public void deleteProduct(Long id) {
    ProductEntity entity = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    entity.setActive(false);
    productRepository.save(entity);
}

@Transactional(readOnly = true)
public java.util.List<Product> getAllActiveProducts() {
    return productRepository.findAll().stream()
            .filter(ProductEntity::isActive)
            .map(productMapper::toDomain)
            .collect(java.util.stream.Collectors.toList());
}
```

- [ ] **Step 2: Create ProductController**

```java
package org.java_avanzado.taller.controller;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getActiveProduct(id));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 3: Compile and Commit**

Run: `./mvnw compile`
Expected: Build SUCCESS

```bash
git add src/main/java/org/java_avanzado/taller/service/ProductService.java src/main/java/org/java_avanzado/taller/controller/ProductController.java
git commit -m "feat(api): complete Product CRUD service and controller"
```

### Task 3: Order Mapper & Service

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/utils/mapper/OrderMapper.java`
- Create: `src/main/java/org/java_avanzado/taller/service/OrderService.java`

- [ ] **Step 1: Create OrderMapper**

```java
package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.OrderProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    @Mapping(target = "orderStatus", source = "status")
    Order toDomain(OrderEntity entity);

    @Mapping(target = "status", source = "orderStatus")
    OrderEntity toEntity(Order domain);

    OrderProduct toDomainProduct(OrderProductEntity entity);
    OrderProductEntity toEntityProduct(OrderProduct domain);
}
```

- [ ] **Step 2: Create OrderService with transaction and locking logic**

```java
package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.repository.OrderRepository;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.utils.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public Order createOrder(UUID userId, List<OrderProduct> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderProduct item : items) {
            Product product = productService.getActiveProduct(Long.parseLong(item.getProductId()));
            product.deductStock(item.getQuantity());

            item.setUnitPrice(product.getPrice());
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            ProductEntity pEntity = productRepository.findById(product.getId()).orElseThrow();
            pEntity.setStockQuantity(product.getQuantity());
            productRepository.save(pEntity); // Optimistic locking check happens on commit
        }

        Order order = Order.builder()
                .userId(userId)
                .totalPrice(total)
                .orderStatus(OrderStatus.APPROVED)
                .orderProducts(items)
                .active(true)
                .build();

        OrderEntity savedEntity = orderRepository.save(orderMapper.toEntity(order));
        return orderMapper.toDomain(savedEntity);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(UUID userId) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getUserId().equals(userId))
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 3: Compile and Commit**

Run: `./mvnw compile`
Expected: Build SUCCESS

```bash
git add src/main/java/org/java_avanzado/taller/utils/mapper/OrderMapper.java src/main/java/org/java_avanzado/taller/service/OrderService.java
git commit -m "feat(service): implement transactional Order creation and retrieval"
```

### Task 4: Order Controller & Audit Implementation

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/controller/OrderController.java`
- Create: `src/main/java/org/java_avanzado/taller/service/EventLogService.java`
- Create: `src/main/java/org/java_avanzado/taller/controller/AuditController.java`

- [ ] **Step 1: Create OrderController**

```java
package org.java_avanzado.taller.controller;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<Order> createOrder(@PathVariable UUID userId, @RequestBody List<OrderProduct> items) {
        return ResponseEntity.ok(orderService.createOrder(userId, items));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable UUID userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}
```

- [ ] **Step 2: Create EventLogService**

```java
package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.java_avanzado.taller.persistence.repository.EventLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventLogService {

    private final EventLogRepository eventLogRepository;

    @Async
    public void logEvent(String eventType, String details, String username) {
        EventLogEntity log = EventLogEntity.builder()
                .eventType(eventType)
                .details(details)
                .username(username)
                .timestamp(LocalDateTime.now())
                .build();
        eventLogRepository.save(log);
    }

    public List<EventLogEntity> getAllLogs() {
        return eventLogRepository.findAll();
    }
}
```

- [ ] **Step 3: Create AuditController**

```java
package org.java_avanzado.taller.controller;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.java_avanzado.taller.service.EventLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final EventLogService eventLogService;

    @GetMapping
    public ResponseEntity<List<EventLogEntity>> getAuditLogs() {
        return ResponseEntity.ok(eventLogService.getAllLogs());
    }
}
```

- [ ] **Step 4: Compile and Commit**

Run: `./mvnw compile`
Expected: Build SUCCESS

```bash
git add src/main/java/org/java_avanzado/taller/controller/ src/main/java/org/java_avanzado/taller/service/EventLogService.java
git commit -m "feat(api): add OrderController, AuditController, and async EventLogService"
```
