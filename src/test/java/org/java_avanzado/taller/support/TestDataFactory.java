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
                .password("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")
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
