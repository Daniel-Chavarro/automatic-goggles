# Domain to Entity Mapping Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix "detached entity", "duplicate insert", and "orphan reference" bugs by implementing the Fetch & Update Strategy using `EntityManager.getReference()` and MapStruct `@MappingTarget`.

**Architecture:** We will introduce a central `ReferenceMapper` that resolves Domain IDs into managed JPA Entity proxies. Then, we will update `OrderMapper`, `ProductMapper`, and `UserMapper` to use these proxies and to support updating existing entities (`@MappingTarget`) instead of instantiating new detached ones. Finally, we will adjust the services to use the new update mapping methods instead of recreating the entities on every save.

**Tech Stack:** Spring Data JPA, MapStruct, Spring Framework (EntityManager)

---

### Task 1: Create Reference Mapper

This component will be used by MapStruct to convert IDs to managed entity proxies, preventing the "detached entity" and "duplicate insert" issues when mapping aggregates like `Product` inside an `Order`.

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/utils/mapper/ReferenceMapper.java`

- [ ] **Step 1: Write the ReferenceMapper implementation**

```java
package org.java_avanzado.taller.utils.mapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReferenceMapper {

    @PersistenceContext
    private EntityManager entityManager;

    public ProductEntity longToProductEntity(Long id) {
        if (id == null) {
            return null;
        }
        return entityManager.getReference(ProductEntity.class, id);
    }

    public UserEntity uuidToUserEntity(UUID id) {
        if (id == null) {
            return null;
        }
        return entityManager.getReference(UserEntity.class, id);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/utils/mapper/ReferenceMapper.java
git commit -m "feat: add ReferenceMapper to resolve entity proxies"
```

### Task 2: Refactor OrderMapper to use ReferenceMapper & Update Targets

Currently, `OrderMapper` explicitly instantiates new entities for its relations (causing detached entity errors) and does not support updating existing `OrderEntity` objects (causing collection orphan issues).

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/utils/mapper/OrderMapper.java`

- [ ] **Step 1: Update OrderMapper interface**

Remove the default methods `stringToProductEntity`, `uuidToUserEntity` and the explicit component model configuration. Delegate them to `ReferenceMapper`. Add `@MappingTarget` methods.

```java
package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.controller.dto.request.create.AddOrderItemRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateOrderRequest;
import org.java_avanzado.taller.controller.dto.response.OrderItemResponse;
import org.java_avanzado.taller.controller.dto.response.OrderResponse;
import org.java_avanzado.taller.controller.dto.response.OrderSummaryResponse;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.OrderProductEntity;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    // Entity <-> Domain
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "orderProducts", target = "orderProducts")
    Order fromOrderEntityToDomain(OrderEntity entity);

    default OrderEntity fromOrderToEntity(Order domain) {
        OrderEntity orderEntity = fromOrderToEntityInternal(domain);
        linkOrderProducts(orderEntity);
        return orderEntity;
    }

    @Mapping(source = "userId", target = "user")
    OrderEntity fromOrderToEntityInternal(Order domain);

    // Fetch & Update Strategy mapping
    @Mapping(source = "userId", target = "user")
    void updateEntityFromDomain(Order domain, @MappingTarget OrderEntity entity);
    
    @AfterMapping
    default void linkOrderProducts(@MappingTarget OrderEntity orderEntity) {
        if (orderEntity != null && orderEntity.getOrderProducts() != null) {
            for (OrderProductEntity item : orderEntity.getOrderProducts()) {
                item.setOrder(orderEntity);
            }
        }
    }

    @Mapping(source = "product.id", target = "productId", qualifiedByName = "longToString")
    OrderProduct fromOrderProductEntityToDomain(OrderProductEntity entity);

    @Mapping(source = "productId", target = "product")
    @Mapping(target = "order", ignore = true)
    OrderProductEntity fromOrderProductToEntity(OrderProduct domain);

    // Domain -> Response 
    OrderResponse fromOrderToResponse(Order order);

    @Mapping(source = "orderStatus", target = "status")
    OrderSummaryResponse fromOrderToSummary(Order order);

    // Order item with product name from context
    @Mapping(source = "productId", target = "productId", qualifiedByName = "stringToLong")
    @Mapping(target = "productName", expression = "java(productNameContext.get(item.getProductId()))")
    OrderItemResponse fromOrderProductToItemResponse(OrderProduct item, @Context Map<String, String> productNameContext);

    // List variants
    List<OrderResponse> fromOrderListToResponseList(List<Order> orders, @Context Map<String, String> productNameContext);
    List<OrderSummaryResponse> fromOrderListToSummaryList(List<Order> orders);

    @Named("longToString")
    default String longToString(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    @Named("stringToLong")
    default Long stringToLong(String value) {
        return value == null ? null : Long.valueOf(value);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/utils/mapper/OrderMapper.java
git commit -m "refactor: update OrderMapper to use ReferenceMapper and add update target"
```

### Task 3: Refactor OrderService to use Fetch & Update Strategy

Update `OrderService` to map mutations directly onto fetched `OrderEntity` instances before saving, rather than persisting unmanaged detached entities built from scratch.

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/service/OrderService.java`

- [ ] **Step 1: Replace save strategy in modification methods**

For `addProductToOrder`, `removeProductFromOrder`, and `modifyQuantityProductInOrder`, we need to get the `OrderEntity`, map the changes, and save. Since the logic updates the `Order` domain model internally via `order.addProduct` etc., we will map the mutated domain model back onto the fetched entity.

Modify `src/main/java/org/java_avanzado/taller/service/OrderService.java`:

Replace the bottom lines in `addProductToOrder`, `removeProductFromOrder`, and `modifyQuantityProductInOrder`:
```java
        // Existing line to replace:
        // OrderEntity updatedEntity = orderRepository.save(orderMapper.fromOrderToEntity(order));
        // return orderMapper.fromOrderEntityToDomain(updatedEntity);
```
with:
```java
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        orderMapper.updateEntityFromDomain(order, entity);
        OrderEntity updatedEntity = orderRepository.save(entity);
        return orderMapper.fromOrderEntityToDomain(updatedEntity);
```

Ensure you do this for:
1. `addProductToOrder`
2. `removeProductFromOrder`
3. `modifyQuantityProductInOrder`

*Note: Ensure to keep `order.addProduct()`, `order.removeProduct()`, etc., so the domain model mutates before mapping it to the entity.*

- [ ] **Step 2: Run build / tests to verify mappings compile and run properly**

```bash
mvn clean compile
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/service/OrderService.java
git commit -m "fix: refactor OrderService to use Fetch & Update mapping strategy"
```

### Task 4: Add Update Mapping targets to Product and User Mappers

Even though `ProductService` and `UserService` use manual setter updates, adding the capability to their mappers ensures consistency across the mapping layer.

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/utils/mapper/ProductMapper.java`
- Modify: `src/main/java/org/java_avanzado/taller/utils/mapper/UserMapper.java`

- [ ] **Step 1: Add update Entity method to ProductMapper**

```java
    // Under Entity <-> Domain section
    @Mapping(source = "quantity", target = "stockQuantity")
    void updateEntityFromDomain(Product domain, @MappingTarget ProductEntity entity);
```

- [ ] **Step 2: Add update Entity method to UserMapper**

```java
    // Under Entity <-> Domain section
    void updateEntityFromDomain(User domain, @MappingTarget UserEntity entity);
```

- [ ] **Step 3: Run full build to ensure no MapStruct compilation errors**

```bash
mvn clean install -DskipTests
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/utils/mapper/ProductMapper.java src/main/java/org/java_avanzado/taller/utils/mapper/UserMapper.java
git commit -m "feat: add updateEntityFromDomain to Product and User mappers"
```
