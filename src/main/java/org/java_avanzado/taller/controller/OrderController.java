package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.CreateOrderRequest;
import org.java_avanzado.taller.controller.dto.response.OrderResponse;
import org.java_avanzado.taller.controller.dto.response.OrderSummaryResponse;
import org.java_avanzado.taller.service.OrderService;
import org.java_avanzado.taller.utils.mapper.OrderResponseMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderResponseMapper orderResponseMapper;

    @PostMapping("/user/{userId}")
    public ResponseEntity<OrderResponse> createOrder(@PathVariable UUID userId, @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponseMapper.toResponse(orderService.createOrder(userId, request)));
    }

    @Transactional(readOnly = true)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderSummaryResponse>> getUserOrders(@PathVariable UUID userId) {
        return ResponseEntity.ok(orderResponseMapper.toSummaryList(orderService.getOrdersByUser(userId)));
    }

    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getAllOrders() {
        return ResponseEntity.ok(orderResponseMapper.toSummaryList(orderService.getAllOrders()));
    }
}