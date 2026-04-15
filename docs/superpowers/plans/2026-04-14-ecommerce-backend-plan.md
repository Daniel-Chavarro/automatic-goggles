# E-Commerce Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a secure, concurrent E-Commerce REST API handling user auth, product inventory, order processing with optimistic locking, and audit logging.

**Architecture:** Domain-Driven Design inspired structure (Controller -> Service -> Domain -> Persistence) with stateless JWT authentication, asynchronous database auditing, and optimistic locking to prevent overselling.

**Tech Stack:** Java 21, Spring Boot 4.0.5, Spring Security, Spring Data JPA, PostgreSQL, H2 (Testing), MapStruct, JJWT.

---

### Task 1: Setup Project Dependencies

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add MapStruct, H2, Validation, and JJWT dependencies to pom.xml**

```xml
        <!-- Add these inside <dependencies> -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.6.0.Beta1</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
```

- [ ] **Step 2: Add MapStruct Annotation Processor**

```xml
                            <!-- Add inside maven-compiler-plugin -> execution -> configuration -> annotationProcessorPaths -->
                                <path>
                                    <groupId>org.mapstruct</groupId>
                                    <artifactId>mapstruct-processor</artifactId>
                                    <version>1.6.0.Beta1</version>
                                </path>
```

- [ ] **Step 3: Verify Maven builds**

Run: `./mvnw clean compile`
Expected: Build SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add pom.xml
git commit -m "chore: add mapstruct, h2, validation, and jjwt dependencies"
```

### Task 2: Implement Domain Exceptions & Business Logic

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/domain/exception/InsufficientStockException.java`
- Modify: `src/main/java/org/java_avanzado/taller/domain/model/Product.java`

- [ ] **Step 1: Create InsufficientStockException**

```java
package org.java_avanzado.taller.domain.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
```

- [ ] **Step 2: Add business logic and missing version to Product model**

```java
// In src/main/java/org/java_avanzado/taller/domain/model/Product.java
// Add missing BigInteger import
import java.math.BigInteger;

// Add version field
    private BigInteger version;

// Add deductStock method
    public void deductStock(int amount) {
        if (this.quantity < amount) {
            throw new org.java_avanzado.taller.domain.exception.InsufficientStockException("Not enough stock for product: " + this.name);
        }
        this.quantity -= amount;
    }
```

- [ ] **Step 3: Compile to ensure no errors**

Run: `./mvnw clean compile`
Expected: Build SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/domain/
git commit -m "feat(domain): add version and deductStock logic to Product"
```

### Task 3: Create EventLog Entity & Repository for Auditing

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/persistence/entity/EventLogEntity.java`
- Create: `src/main/java/org/java_avanzado/taller/persistence/repository/EventLogRepository.java`

- [ ] **Step 1: Create EventLogEntity**

```java
package org.java_avanzado.taller.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType; // e.g., "LOGIN_SUCCESS", "LOGIN_FAILED", "UNAUTHORIZED_ACCESS"

    @Column(nullable = false)
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column
    private String username;
}
```

- [ ] **Step 2: Create EventLogRepository**

```java
package org.java_avanzado.taller.persistence.repository;

import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<EventLogEntity, Long> {
}
```

- [ ] **Step 3: Compile**

Run: `./mvnw clean compile`
Expected: Build SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/persistence/
git commit -m "feat(persistence): add EventLog for audit events"
```

### Task 4: Implement MapStruct Mappers

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/utils/mapper/ProductMapper.java`

- [ ] **Step 1: Create ProductMapper**

```java
package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    @Mapping(source = "stockQuantity", target = "quantity")
    Product toDomain(ProductEntity entity);

    @Mapping(source = "quantity", target = "stockQuantity")
    ProductEntity toEntity(Product domain);
}
```

- [ ] **Step 2: Compile to trigger MapStruct generation**

Run: `./mvnw clean compile`
Expected: Build SUCCESS. You should see generated mappers in `target/generated-sources/annotations`.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/utils/mapper/
git commit -m "feat(utils): add ProductMapper using MapStruct"
```

### Task 5: Implement ProductService

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/service/ProductService.java`

- [ ] **Step 1: Write ProductService class**

```java
package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.utils.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Product getActiveProduct(Long id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (!entity.isActive()) {
            throw new RuntimeException("Product is disabled");
        }
        return productMapper.toDomain(entity);
    }
}
```

- [ ] **Step 2: Verify Compilation**

Run: `./mvnw compile`
Expected: Build SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/service/ProductService.java
git commit -m "feat(service): implement ProductService retrieval logic"
```

### Task 6: Global Exception Handler

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/controller/advice/GlobalExceptionHandler.java`

- [ ] **Step 1: Implement GlobalExceptionHandler**

```java
package org.java_avanzado.taller.controller.advice;

import org.java_avanzado.taller.domain.exception.InsufficientStockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<?> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<?> handleConcurrencyError(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Resource was updated by another transaction. Please try again."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }
}
```

- [ ] **Step 2: Compile**

Run: `./mvnw compile`
Expected: Build SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/advice/
git commit -m "feat(controller): add GlobalExceptionHandler for domain and optimistic locking errors"
```
