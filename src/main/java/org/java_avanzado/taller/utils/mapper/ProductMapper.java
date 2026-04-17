package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateProductRequest;
import org.java_avanzado.taller.controller.dto.response.ProductResponse;
import org.java_avanzado.taller.controller.dto.response.ProductSummaryResponse;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    // Entity <-> Domain
    @Mapping(source = "stockQuantity", target = "quantity")
    Product fromProductEntityToDomain(ProductEntity entity);

    @Mapping(source = "quantity", target = "stockQuantity")
    ProductEntity fromProductToEntity(Product domain);

    // Create Request -> Domain
    Product fromCreateProductRequestToDomain(CreateProductRequest request);

    // Update Request -> Domain (merge with existing)
    @Mapping(source = "existingProduct.id", target = "id")
    @Mapping(source = "existingProduct.version", target = "version")
    @Mapping(source = "existingProduct.active", target = "active")
    @Mapping(source = "request.name", target = "name")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.price", target = "price")
    @Mapping(source = "request.quantity", target = "quantity")
    Product fromUpdateProductRequestToDomain(UpdateProductRequest request, Product existingProduct);

    // Domain -> Response
    ProductResponse fromProductToResponse(Product product);
    ProductSummaryResponse fromProductToSummary(Product product);

    // List variants
    List<ProductResponse> fromProductListToResponseList(List<Product> products);
    List<ProductSummaryResponse> fromProductListToSummaryList(List<Product> products);
}
