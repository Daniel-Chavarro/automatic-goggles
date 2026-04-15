# DTO Design for Controller Layer

**Date:** 2026-04-15  
**Status:** Approved

## Overview

Add request and response DTOs to all controllers (User, Product, Order) to:
- Hide internal fields (password, version, etc.)
- Add validation constraints to request bodies
- Follow SOLID principles (separation of concerns)

## Package Structure

```
controller/dto/
├── request/
│   ├── create/
│   │   ├── CreateUserRequest.java
│   │   ├── CreateProductRequest.java
│   │   ├── CreateOrderRequest.java
│   │   └── AddOrderItemRequest.java
│   └── update/
│       ├── UpdateUserRequest.java
│       ├── UpdateProductRequest.java
│       └── UpdateOrderRequest.java (if needed)
└── response/
    ├── UserResponse.java
    ├── UserSummaryResponse.java
    ├── ProductResponse.java
    ├── ProductSummaryResponse.java
    ├── OrderResponse.java
    ├── OrderSummaryResponse.java
    └── OrderItemResponse.java
```

## DTO Specifications

### 1. User DTOs

#### Request DTOs

**CreateUserRequest**
- Extends: `RegisterUserRequest` (firstName, lastName, email, phone, password)
- Additional field: `role` (UserRole) — admin only

**UpdateUserRequest**
- firstName (String, optional)
- lastName (String, optional)
- phone (String, optional)
- Validation: same constraints as RegisterUserRequest

#### Response DTOs

**UserResponse**
- id (UUID)
- firstName (String)
- lastName (String)
- email (String)
- phone (String, nullable)
- role (UserRole)
- active (boolean)
- createdAt (LocalDateTime)
- Excludes: password, version

**UserSummaryResponse**
- id (UUID)
- firstName (String)
- lastName (String)
- email (String)

---

### 2. Product DTOs

#### Request DTOs

**CreateProductRequest**
- name (String, @NotBlank, max 255)
- description (String, optional, max 1000)
- price (BigDecimal, @NotNull, @Positive)
- quantity (Integer, @NotNull, @Min(0))

**UpdateProductRequest**
- name (String, optional)
- description (String, optional)
- price (BigDecimal, optional, @Positive)
- quantity (Integer, optional, @Min(0))

#### Response DTOs

**ProductResponse**
- id (Long)
- name (String)
- description (String, nullable)
- price (BigDecimal)
- stock (Integer) — renamed from quantity
- active (boolean)
- createdAt (LocalDateTime)
- Excludes: version

**ProductSummaryResponse**
- id (Long)
- name (String)
- price (BigDecimal)

---

### 3. Order DTOs

#### Request DTOs

**AddOrderItemRequest**
- productId (Long, @NotNull)
- quantity (Integer, @NotNull, @Min(1))

**CreateOrderRequest**
- items (List<AddOrderItemRequest>, @NotEmpty, @Valid)

**UpdateOrderRequest** (optional)
- status (OrderStatus, optional) — for admin to update status

#### Response DTOs

**OrderItemResponse**
- productId (Long)
- productName (String)
- quantity (Integer)
- unitPrice (BigDecimal)

**OrderResponse**
- id (Long)
- userId (UUID)
- totalPrice (BigDecimal)
- status (OrderStatus)
- items (List<OrderItemResponse>)
- active (boolean)
- createdAt (LocalDateTime)

**OrderSummaryResponse**
- id (Long)
- totalPrice (BigDecimal)
- status (OrderStatus)
- createdAt (LocalDateTime)

---

## Implementation Notes

1. Use Lombok annotations (`@Data`, `@Builder`) for all DTOs
2. Add `@JsonInclude(JsonInclude.Include.NON_NULL)` for responses
3. Use existing mappers (UserMapper, ProductMapper, OrderMapper) or create new ones
4. Update controllers to use DTOs instead of domain models
5. Add `@Valid` annotation to request body parameters

## Controller Changes Required

1. **UserController** — Return `UserResponse` instead of `User`
2. **ProductController** — Use `CreateProductRequest`/`UpdateProductRequest` for input, return `ProductResponse`
3. **OrderController** — Use `CreateOrderRequest` for input, return `OrderResponse`

## Success Criteria

- [ ] No domain models exposed in API responses
- [ ] Request validation constraints applied to all inputs
- [ ] Controllers return proper DTOs
- [ ] Mappers updated to convert between domain and DTOs
- [ ] Tests updated to validate DTO structure