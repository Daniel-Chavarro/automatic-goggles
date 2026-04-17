package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.controller.dto.request.create.AddOrderItemRequest;
import org.java_avanzado.taller.controller.dto.request.create.CreateOrderRequest;
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
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    // Entity <-> Domain
    @Mapping(source = "orderProducts", target = "orderProducts")
    Order fromOrderEntityToDomain(OrderEntity entity);

    default OrderEntity fromOrderToEntity(Order domain) {
        OrderEntity orderEntity = fromOrderToEntityInternal(domain);
        if (orderEntity == null || orderEntity.getOrderProducts() == null) {
            return orderEntity;
        }
        for (OrderProductEntity item : orderEntity.getOrderProducts()) {
            item.setOrder(orderEntity);
        }
        return orderEntity;
    }

    @Mapping(source = "userId", target = "user", qualifiedByName = "uuidToUserEntity")
    OrderEntity fromOrderToEntityInternal(Order domain);

    @Mapping(source = "product.id", target = "productId", qualifiedByName = "longToString")
    OrderProduct fromOrderProductEntityToDomain(OrderProductEntity entity);

    @Mapping(source = "productId", target = "product", qualifiedByName = "stringToProductEntity")
    @Mapping(target = "order", ignore = true)
    OrderProductEntity fromOrderProductToEntity(OrderProduct domain);

    // Create Request -> Domain
    Order fromCreateOrderRequestToDomain(CreateOrderRequest request);
    OrderProduct fromAddOrderItemRequestToDomain(AddOrderItemRequest request);

    // Update Request -> Domain
    @Mapping(source = "existingOrder.id", target = "id")
    @Mapping(source = "existingOrder.userId", target = "userId")
    @Mapping(source = "existingOrder.totalPrice", target = "totalPrice")
    @Mapping(source = "request.status", target = "orderStatus")
    @Mapping(source = "existingOrder.orderProducts", target = "orderProducts")
    @Mapping(source = "existingOrder.active", target = "active")
    Order fromUpdateOrderRequestToDomain(UpdateOrderRequest request, Order existingOrder);

    // Domain -> Response (with additional parameter for product name)
    @Mapping(source = "orderStatus", target = "status")
    @Mapping(source = "orderProducts", target = "items")
    OrderResponse fromOrderToResponse(Order order, @Context Map<String, String> productNameContext);

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

    @Named("uuidToUserEntity")
    default UserEntity uuidToUserEntity(UUID userId) {
        if (userId == null) {
            return null;
        }
        UserEntity user = new UserEntity();
        user.setId(userId);
        return user;
    }

    @Named("stringToProductEntity")
    default ProductEntity stringToProductEntity(String productId) {
        if (productId == null) {
            return null;
        }
        ProductEntity product = new ProductEntity();
        product.setId(Long.valueOf(productId));
        return product;
    }

}
