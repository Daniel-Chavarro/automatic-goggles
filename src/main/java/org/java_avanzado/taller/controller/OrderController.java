package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.CreateOrderRequest;
import org.java_avanzado.taller.controller.dto.response.OrderResponse;
import org.java_avanzado.taller.controller.dto.response.OrderSummaryResponse;
import org.java_avanzado.taller.service.OrderService;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.utils.mapper.OrderMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Exposes order management endpoints.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final OrderMapper orderMapper;

    /**
     * Creates a new order for the given user.
     *
     * @param userId user identifier
     * @param request validated order payload
     * @return the created order with resolved product names
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<OrderResponse> createOrder(@PathVariable UUID userId, @Valid @RequestBody CreateOrderRequest request) {
        Map<String, String> productNames = buildProductNameMap(request.getItems().stream()
                .map(item -> item.getProductId())
                .toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.fromOrderToResponse(orderService.createOrder(userId, request), productNames));
    }

    /**
     * Returns the orders associated with a user.
     *
     * @param userId user identifier
     * @return the user order summaries
     */
    @Transactional(readOnly = true)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderSummaryResponse>> getUserOrders(@PathVariable UUID userId) {
        return ResponseEntity.ok(orderMapper.fromOrderListToSummaryList(orderService.getOrdersByUser(userId)));
    }

    /**
     * Returns all orders in the system.
     *
     * @return the order summaries
     */
    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getAllOrders() {
        return ResponseEntity.ok(orderMapper.fromOrderListToSummaryList(orderService.getAllOrders()));
    }

    private Map<String, String> buildProductNameMap(List<Long> productIds) {
        Map<String, String> map = new HashMap<>();
        for (Long id : productIds) {
            try {
                String name = productService.getActiveProduct(id).getName();
                map.put(String.valueOf(id), name);
            } catch (Exception e) {
                map.put(String.valueOf(id), "Unknown");
            }
        }
        return map;
    }
}