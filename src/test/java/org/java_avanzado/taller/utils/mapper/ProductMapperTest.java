package org.java_avanzado.taller.utils.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.support.TestDataFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Nested
    class GivenProductEntity {

        @Test
        void when_mappingToDomain_then_stockQuantityIsMappedAsQuantity() {
            var entity = TestDataFactory.productEntity();
            entity.setStockQuantity(42);

            var result = mapper.fromProductEntityToDomain(entity);

            assertEquals(42, result.getQuantity());
            assertEquals(entity.getId(), result.getId());
            assertEquals(entity.getName(), result.getName());
        }
    }

    @Nested
    class GivenProductDomain {

        @Test
        void when_mappingToResponse_then_quantityIsMappedAsStock() {
            Product product = TestDataFactory.product();

            var result = mapper.fromProductToResponse(product);

            assertEquals(product.getQuantity(), result.getStock());
        }
    }

    @Nested
    class GivenUpdateRequestAndExistingProduct {

        @Test
        void when_mappingToDomain_then_ignoredFieldsArePreservedAndUpdatableFieldsAreApplied() {
            var request = TestDataFactory.updateProductRequest();
            Product existing = Product.builder()
                    .id(77L)
                    .name("Old Name")
                    .description("Old description")
                    .price(new BigDecimal("9.99"))
                    .quantity(3)
                    .active(true)
                    .version(new BigInteger("12"))
                    .build();

            var result = mapper.fromUpdateProductRequestToDomain(request, existing);

            assertEquals(existing.getId(), result.getId());
            assertEquals(existing.getVersion(), result.getVersion());
            assertEquals(existing.isActive(), result.isActive());
            assertEquals(request.getName(), result.getName());
            assertEquals(request.getDescription(), result.getDescription());
            assertEquals(request.getPrice(), result.getPrice());
            assertEquals(request.getQuantity(), result.getQuantity());
        }
    }
}
