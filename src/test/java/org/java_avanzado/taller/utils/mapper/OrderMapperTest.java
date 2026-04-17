package org.java_avanzado.taller.utils.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.java_avanzado.taller.controller.dto.request.update.UpdateOrderRequest;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.domain.model.OrderStatus;
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

        @Test
        void when_mappingToResponseWithNonNumericProductId_then_numberFormatExceptionIsThrown() {
            Order order = Order.builder()
                    .id(9L)
                    .userId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .totalPrice(new BigDecimal("25.00"))
                    .orderStatus(OrderStatus.APPROVED)
                    .orderProducts(List.of(OrderProduct.builder()
                            .id(2L)
                            .productId("ABC")
                            .quantity(2)
                            .unitPrice(new BigDecimal("12.50"))
                            .build()))
                    .active(true)
                    .build();
            Map<String, String> productNameContext = Map.of("ABC", "Coffee");

            assertThrows(NumberFormatException.class, () -> mapper.fromOrderToResponse(order, productNameContext));
        }
    }

    @Nested
    class GivenUpdateRequestAndExistingOrder {

        @Test
        void when_mappingToDomain_then_statusIsUpdatedAndProtectedFieldsArePreserved() {
            UpdateOrderRequest request = new UpdateOrderRequest();
            request.setStatus(OrderStatus.REJECTED);

            Order existing = Order.builder()
                    .id(9L)
                    .userId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .totalPrice(new BigDecimal("25.00"))
                    .orderStatus(OrderStatus.APPROVED)
                    .orderProducts(List.of(TestDataFactory.orderProduct()))
                    .active(true)
                    .build();

            var result = mapper.fromUpdateOrderRequestToDomain(request, existing);

            assertEquals(request.getStatus(), result.getOrderStatus());
            assertEquals(existing.getId(), result.getId());
            assertEquals(existing.getUserId(), result.getUserId());
            assertEquals(existing.getTotalPrice(), result.getTotalPrice());
            assertEquals(existing.getOrderProducts(), result.getOrderProducts());
            assertEquals(existing.isActive(), result.isActive());
        }
    }
}
