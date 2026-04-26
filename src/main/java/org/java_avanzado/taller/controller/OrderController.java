package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.AddOrderItemRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateOrderItemRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateOrderRequest;
import org.java_avanzado.taller.controller.dto.response.OrderResponse;
import org.java_avanzado.taller.controller.dto.response.OrderSummaryResponse;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.service.OrderService;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.utils.mapper.OrderMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @return the created order with resolved product names
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<OrderResponse> createOrder(@PathVariable @Valid UUID userId) {
        Order order = orderService.createOrder(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.fromOrderToResponse(order));
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

    /**
     * Returns an order by identifier.
     *
     * @param orderId order identifier
     * @return the order details with resolved product names
     */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds a product to an existing order, updating the order total and product stock accordingly.
     *
     * @param orderId the identifier of the order to which the product will be added
     * @param request the request containing the product identifier and quantity to add
     * @return the updated order with resolved product names
     */
    @PostMapping("/{id}/items")
    @Transactional
    public ResponseEntity<OrderResponse> addProductToOrder(@PathVariable("id") Long orderId,
                                                           @Valid AddOrderItemRequest request) {
        Order updatedOrder = orderService.addProductToOrder(orderId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(orderMapper.fromOrderToResponse(updatedOrder));
    }

    /**
     * Modifies the quantity of a product in an existing order, updating the order total and product stock accordingly.
     *
     * @param orderId the identifier of the order to which the product will be added
     * @param request the request containing the product identifier and new quantity to set
     * @return the updated order with resolved product names
     */
    @PutMapping("/{id}/items")
    @Transactional
    public ResponseEntity<OrderResponse> modifyQuantityOrderProduct(
            @PathVariable("id") Long orderId,
            @Valid UpdateOrderItemRequest request) {
        Order updatedOrder = orderService.modifyQuantityProductInOrder(
                orderId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(orderMapper.fromOrderToResponse(updatedOrder));
    }


    /**
     * Removes a product from an existing order, updating the order total and product stock accordingly.
     *
     * @param orderId the identifier of the order from which to remove the product
     * @param productId the identifier of the product to remove
     * @return the updated order with resolved product names
     */
    @Transactional
    @DeleteMapping("/{order-id}/items/{product-id}")
    public ResponseEntity<OrderResponse> deleteItemFromOrder(
            @PathVariable("order-id") Long orderId,
            @PathVariable("product-id") Long productId) {
        return ResponseEntity.ok(orderMapper.fromOrderToResponse(
                orderService.removeProductFromOrder(orderId, productId)));
    }
}