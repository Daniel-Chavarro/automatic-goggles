package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
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
    public Order createOrder(UUID userId, List<OrderProduct> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderProduct item : items) {
            long productId;
            try {
                productId = Long.parseLong(item.getProductId());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid product ID: " + item.getProductId());
            }

            ProductEntity pEntity = productRepository.findByIdAndActiveTrue(productId)
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

            item.setUnitPrice(product.getPrice());
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            pEntity.setStockQuantity(product.getQuantity());
            productRepository.save(pEntity);
        }

        Order order = Order.builder()
                .userId(userId)
                .totalPrice(total)
                .orderStatus(OrderStatus.APPROVED)
                .orderProducts(items)
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