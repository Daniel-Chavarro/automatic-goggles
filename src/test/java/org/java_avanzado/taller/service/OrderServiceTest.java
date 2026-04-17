package org.java_avanzado.taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.java_avanzado.taller.domain.exception.InsufficientStockException;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.repository.OrderRepository;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.utils.mapper.OrderMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Nested
    class GivenCreateOrder {

        @Test
        void given_availableProducts_when_createOrder_then_returnsMappedSavedOrder() {
            UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            var request = TestDataFactory.createOrderRequest();

            ProductEntity productEntity = TestDataFactory.productEntity();
            OrderEntity mappedOrderEntity = TestDataFactory.orderEntity();
            OrderEntity savedOrderEntity = TestDataFactory.orderEntity();
            Order expectedOrder = TestDataFactory.order();

            when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(productEntity));
            when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
            when(orderMapper.fromOrderToEntity(any(Order.class))).thenReturn(mappedOrderEntity);
            when(orderRepository.save(mappedOrderEntity)).thenReturn(savedOrderEntity);
            when(orderMapper.fromOrderEntityToDomain(savedOrderEntity)).thenReturn(expectedOrder);

            Order result = orderService.createOrder(userId, request);

            assertSame(expectedOrder, result);

            verify(orderRepository).save(mappedOrderEntity);
            verify(orderMapper).fromOrderEntityToDomain(savedOrderEntity);
        }

        @Test
        void given_availableProducts_when_createOrder_then_decrementsProductStockBeforeSaving() {
            UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            var request = TestDataFactory.createOrderRequest();

            ProductEntity productEntity = TestDataFactory.productEntity();
            OrderEntity mappedOrderEntity = TestDataFactory.orderEntity();
            OrderEntity savedOrderEntity = TestDataFactory.orderEntity();

            when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(productEntity));
            when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
            when(orderMapper.fromOrderToEntity(any(Order.class))).thenReturn(mappedOrderEntity);
            when(orderRepository.save(mappedOrderEntity)).thenReturn(savedOrderEntity);
            when(orderMapper.fromOrderEntityToDomain(savedOrderEntity)).thenReturn(TestDataFactory.order());

            orderService.createOrder(userId, request);

            ArgumentCaptor<ProductEntity> productCaptor = ArgumentCaptor.forClass(ProductEntity.class);
            verify(productRepository).save(productCaptor.capture());
            assertEquals(18, productCaptor.getValue().getStockQuantity());
        }

        @Test
        void given_availableProducts_when_createOrder_then_buildsOrderWithRequestData() {
            UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            var request = TestDataFactory.createOrderRequest();

            ProductEntity productEntity = TestDataFactory.productEntity();
            OrderEntity mappedOrderEntity = TestDataFactory.orderEntity();
            OrderEntity savedOrderEntity = TestDataFactory.orderEntity();

            when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(productEntity));
            when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
            when(orderMapper.fromOrderToEntity(any(Order.class))).thenReturn(mappedOrderEntity);
            when(orderRepository.save(mappedOrderEntity)).thenReturn(savedOrderEntity);
            when(orderMapper.fromOrderEntityToDomain(savedOrderEntity)).thenReturn(TestDataFactory.order());

            orderService.createOrder(userId, request);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderMapper).fromOrderToEntity(orderCaptor.capture());
            Order builtOrder = orderCaptor.getValue();
            assertEquals(userId, builtOrder.getUserId());
            assertEquals(new BigDecimal("25.00"), builtOrder.getTotalPrice());
            assertEquals(1, builtOrder.getOrderProducts().size());
        }

        @Test
        void given_missingOrDisabledProduct_when_createOrder_then_throwsIllegalArgumentException() {
            UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            var request = TestDataFactory.createOrderRequest();

            when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(userId, request));

            verify(productRepository).findByIdAndActiveTrue(1L);
            verifyNoInteractions(orderRepository, orderMapper);
        }

        @Test
        void given_insufficientStock_when_createOrder_then_throwsInsufficientStockException() {
            UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            var request = TestDataFactory.createOrderRequest();

            ProductEntity productEntity = TestDataFactory.productEntity();
            productEntity.setStockQuantity(1);

            when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(productEntity));

            assertThrows(InsufficientStockException.class, () -> orderService.createOrder(userId, request));

            verify(productRepository).findByIdAndActiveTrue(1L);
            verify(productRepository, times(0)).save(any(ProductEntity.class));
            verifyNoInteractions(orderRepository, orderMapper);
        }
    }

    @Nested
    class GivenGetOrdersByUser {

        @Test
        void given_repositoryResults_when_getOrdersByUser_then_returnsMappedOrders() {
            UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            OrderEntity firstEntity = TestDataFactory.orderEntity();
            OrderEntity secondEntity = TestDataFactory.orderEntity();
            secondEntity.setId(10L);

            Order firstOrder = TestDataFactory.order();
            Order secondOrder = TestDataFactory.order();
            secondOrder.setId(10L);

            when(orderRepository.findAllByUserId(userId)).thenReturn(List.of(firstEntity, secondEntity));
            when(orderMapper.fromOrderEntityToDomain(firstEntity)).thenReturn(firstOrder);
            when(orderMapper.fromOrderEntityToDomain(secondEntity)).thenReturn(secondOrder);

            List<Order> result = orderService.getOrdersByUser(userId);

            assertEquals(2, result.size());
            assertSame(firstOrder, result.get(0));
            assertSame(secondOrder, result.get(1));
            verify(orderRepository).findAllByUserId(userId);
            verify(orderMapper).fromOrderEntityToDomain(firstEntity);
            verify(orderMapper).fromOrderEntityToDomain(secondEntity);
        }
    }
}
