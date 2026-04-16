package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.AddOrderItemRequest;
import org.java_avanzado.taller.controller.dto.request.create.CreateOrderRequest;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.domain.model.OrderStatus;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.repository.OrderRepository;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.utils.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public Order createOrder(UUID userId, CreateOrderRequest request) {
        BigDecimal total = BigDecimal.ZERO;

        for (AddOrderItemRequest item : request.getItems()) {
            ProductEntity pEntity = productRepository.findByIdAndActiveTrue(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found or disabled"));

            Product product = Product.builder()
                    .id(pEntity.getId())
                    .name(pEntity.getName())
                    .description(pEntity.getDescription())
                    .price(pEntity.getPrice())
                    .quantity(pEntity.getStockQuantity())
                    .active(pEntity.isActive())
                    .build();

            product.deductStock(item.getQuantity());

            OrderProduct orderProduct = OrderProduct.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            pEntity.setStockQuantity(product.getQuantity());
            productRepository.save(pEntity);
        }

        List<OrderProduct> orderItems = request.getItems().stream()
                .map(item -> {
                    ProductEntity pEntity = productRepository.findByIdAndActiveTrue(item.getProductId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found or disabled"));
                    return OrderProduct.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .unitPrice(pEntity.getPrice())
                            .build();
                })
                .collect(Collectors.toList());

        Order order = Order.builder()
                .userId(userId)
                .totalPrice(total)
                .orderStatus(OrderStatus.APPROVED)
                .orderProducts(orderItems)
                .active(true)
                .build();

        OrderEntity savedEntity = orderRepository.save(orderMapper.toEntity(order));
        return orderMapper.toDomain(savedEntity);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(UUID userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }
}