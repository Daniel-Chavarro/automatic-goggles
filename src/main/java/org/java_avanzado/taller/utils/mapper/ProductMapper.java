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
