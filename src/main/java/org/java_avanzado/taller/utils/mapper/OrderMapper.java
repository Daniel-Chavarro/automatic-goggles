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
