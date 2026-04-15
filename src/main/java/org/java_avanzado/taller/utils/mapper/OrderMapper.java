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
    Order toDomain(OrderEntity entity);

    OrderEntity toEntity(Order domain);

    OrderProduct toDomainProduct(OrderProductEntity entity);
    OrderProductEntity toEntityProduct(OrderProduct domain);
}