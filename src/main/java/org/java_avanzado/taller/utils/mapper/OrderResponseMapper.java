package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.controller.dto.response.OrderItemResponse;
import org.java_avanzado.taller.controller.dto.response.OrderResponse;
import org.java_avanzado.taller.controller.dto.response.OrderSummaryResponse;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.service.ProductService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderResponseMapper {
    
    private final ProductService productService;
    
    public OrderResponseMapper(ProductService productService) {
        this.productService = productService;
    }
    
    public OrderItemResponse itemToResponse(OrderProduct item) {
        String productName = productService.getActiveProduct(item.getProductId()).getName();
        return OrderItemResponse.builder()
                .productId(item.getProductId())
                .productName(productName)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
    }
    
    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalPrice(order.getTotalPrice())
                .status(order.getOrderStatus())
                .items(order.getOrderProducts().stream()
                        .map(this::itemToResponse)
                        .collect(Collectors.toList()))
                .active(order.isActive())
                .build();
    }
    
    public OrderSummaryResponse toSummary(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getOrderStatus())
                .build();
    }
    
    public List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<OrderSummaryResponse> toSummaryList(List<Order> orders) {
        return orders.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }
}
