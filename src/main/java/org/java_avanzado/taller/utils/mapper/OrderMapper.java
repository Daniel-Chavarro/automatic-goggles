package org.java_avanzado.taller.utils.mapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.java_avanzado.taller.controller.dto.response.OrderItemResponse;
import org.java_avanzado.taller.controller.dto.response.OrderResponse;
import org.java_avanzado.taller.controller.dto.response.OrderSummaryResponse;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.OrderProductEntity;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "orderProducts", target = "orderProducts")
    Order fromOrderEntityToDomain(OrderEntity entity);

    default OrderEntity fromOrderToEntity(Order domain) {
        return fromOrderToEntityInternal(domain);
    }

    @Mapping(source = "userId", target = "user")
    OrderEntity fromOrderToEntityInternal(Order domain);

    @AfterMapping
    default void linkOrderProducts(@MappingTarget OrderEntity orderEntity) {
        if (orderEntity != null && orderEntity.getOrderProducts() != null) {
            for (OrderProductEntity item : orderEntity.getOrderProducts()) {
                item.setOrder(orderEntity);
            }
        }
    }

    @Mapping(source = "userId", target = "user")
    void updateEntityFromDomain(Order domain, @MappingTarget OrderEntity entity);

    @Mapping(source = "product.id", target = "productId", qualifiedByName = "longToString")
    OrderProduct fromOrderProductEntityToDomain(OrderProductEntity entity);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", expression = "java(referenceMapper.longToProductEntity(domain.getProductId()))")
    OrderProductEntity fromOrderProductToEntity(OrderProduct domain, @Context ReferenceMapper referenceMapper);

    @Mapping(source = "orderStatus", target = "status")
    @Mapping(target = "items", ignore = true)
    OrderResponse fromOrderToResponse(Order order);

    @AfterMapping
    default void populateOrderItems(Order order, @MappingTarget OrderResponse response, @Context EntityManager entityManager) {
        if (order == null || order.getOrderProducts() == null || order.getOrderProducts().isEmpty()) {
            return;
        }

        List<OrderItemResponse> items = new ArrayList<>();
        for (OrderProduct op : order.getOrderProducts()) {
            String productName = null;
            if (op.getProductId() != null) {
                ProductEntity product = entityManager.getReference(ProductEntity.class, op.getProductId());
                if (product != null) {
                    productName = product.getName();
                }
            }

            items.add(OrderItemResponse.builder()
                    .productId(op.getProductId())
                    .productName(productName)
                    .quantity(op.getQuantity())
                    .unitPrice(op.getUnitPrice())
                    .build());
        }

        response.setItems(items);
    }

    @Mapping(source = "orderStatus", target = "status")
    OrderSummaryResponse fromOrderToSummary(Order order);

    @Mapping(source = "productId", target = "productId", qualifiedByName = "stringToLong")
    @Mapping(target = "productName", expression = "java(productNameContext.get(item.getProductId()))")
    OrderItemResponse fromOrderProductToItemResponse(OrderProduct item, @Context Map<String, String> productNameContext);

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

    @Named("stringToProductEntity")
    default ProductEntity stringToProductEntity(String productId, @Context ReferenceMapper referenceMapper) {
        if (productId == null) {
            return null;
        }
        return referenceMapper.longToProductEntity(Long.parseLong(productId));
    }

    @Named("longToProductEntity")
    default ProductEntity longToProductEntity(Long productId, @Context ReferenceMapper referenceMapper) {
        if (productId == null) {
            return null;
        }
        return referenceMapper.longToProductEntity(productId);
    }
}