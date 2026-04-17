package org.java_avanzado.taller.utils.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.support.TestDataFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class OrderMapperTest {

    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Nested
    class GivenOrderEntity {

        @Test
        void when_mappingToDomain_then_statusAndItemListAreMapped() {
            var entity = TestDataFactory.orderEntity();

            var result = mapper.fromOrderEntityToDomain(entity);

            assertEquals(entity.getOrderStatus(), result.getOrderStatus());
            assertEquals(entity.getOrderProducts().size(), result.getOrderProducts().size());
            assertEquals(
                    String.valueOf(entity.getOrderProducts().get(0).getProduct().getId()),
                    result.getOrderProducts().get(0).getProductId());
        }
    }

    @Nested
    class GivenOrderDomainAndProductNameContext {

        @Test
        void when_mappingToResponse_then_itemProductNameIsResolvedAndProductIdIsConvertedToLong() {
            Order order = TestDataFactory.order();
            Map<String, String> productNameContext = Map.of("1", "Coffee");

            var result = mapper.fromOrderToResponse(order, productNameContext);

            assertEquals(order.getOrderStatus(), result.getStatus());
            assertEquals(order.getOrderProducts().size(), result.getItems().size());
            assertEquals(1L, result.getItems().get(0).getProductId());
            assertEquals("Coffee", result.getItems().get(0).getProductName());
        }
    }
}
