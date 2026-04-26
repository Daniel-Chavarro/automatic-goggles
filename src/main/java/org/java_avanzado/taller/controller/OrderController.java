package org.java_avanzado.taller.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.AddOrderItemRequest;
import org.java_avanzado.taller.controller.dto.request.filter.OrderFilterDto;
import org.java_avanzado.taller.controller.dto.request.update.UpdateOrderItemRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateOrderRequest;
import org.java_avanzado.taller.controller.dto.response.OrderResponse;
import org.java_avanzado.taller.controller.dto.response.OrderSummaryResponse;
import org.java_avanzado.taller.controller.dto.response.PaginatedResponse;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.service.OrderService;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.utils.mapper.OrderMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Orders", description = "Order management operations")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final OrderMapper orderMapper;

    @Operation(summary = "Create a new order", description = "Creates a new order for the specified user. " +
            "Initially empty, items must be added separately." +
            "\nRequires ADMIN OR CLIENT role")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PostMapping("/user/{userId}")
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "Unique identifier of the user placing the order", required = true) @PathVariable @Valid UUID userId) {
        Order order = orderService.createOrder(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.fromOrderToResponse(order));
    }

    @Operation(summary = "Get user orders",
            description = "Returns a paginated list of orders for a specific user with optional filtering." +
                    "\nRequires ADMIN OR CLIENT role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @Transactional(readOnly = true)
    @GetMapping("/user/{userId}")
    public ResponseEntity<PaginatedResponse<OrderSummaryResponse>> getUserOrders(
            @Parameter(description = "User identifier to filter orders", required = true) @PathVariable UUID userId,
            @Parameter(description = "Filter criteria for orders") OrderFilterDto filter,
            @Parameter(description = "Pagination and sorting information") Pageable pageable) {
        filter.setUserId(userId);
        Page<Order> orders = orderService.getOrders(filter, pageable);
        Page<OrderSummaryResponse> responsePage = orders.map(orderMapper::fromOrderToSummary);
        return ResponseEntity.ok(PaginatedResponse.from(responsePage));
    }

    @Operation(summary = "Get all orders",
            description = "Returns a paginated list of all orders in the system with optional filtering." +
                    "\nRequires ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<PaginatedResponse<OrderSummaryResponse>> getAllOrders(
            @Parameter(description = "Filter criteria for orders") OrderFilterDto filter,
            @Parameter(description = "Pagination and sorting information") Pageable pageable) {
        Page<Order> orders = orderService.getOrders(filter, pageable);
        Page<OrderSummaryResponse> responsePage = orders.map(orderMapper::fromOrderToSummary);
        return ResponseEntity.ok(PaginatedResponse.from(responsePage));
    }

    @Operation(summary = "Delete an order", description = "Permanently removes an order from the system. This action cannot be undone.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "Unique identifier of the order to delete", required = true) @PathVariable("id") Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add product to order",
            description = "Adds a product to an existing order with the specified quantity. " +
                    "Updates order total and product stock." +
                    "\nRequires ADMIN OR CLIENT role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product added to order successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order or product not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Insufficient stock or order already finished", content = @Content)
    })
    @PostMapping("/{id}/items")
    @Transactional
    public ResponseEntity<OrderResponse> addProductToOrder(
            @Parameter(description = "Unique identifier of the order", required = true) @PathVariable("id") Long orderId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product and quantity to add",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AddOrderItemRequest.class)))
            @Valid @RequestBody AddOrderItemRequest request) {
        Order updatedOrder = orderService.addProductToOrder(orderId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(orderMapper.fromOrderToResponse(updatedOrder));
    }

    @Operation(summary = "Update product quantity in order",
            description = "Modifies the quantity of a specific product in an existing order. " +
                    "Updates order total and product stock accordingly." +
                    "\nRequires ADMIN OR CLIENT role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product quantity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order or product not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Insufficient stock or order already finished", content = @Content)
    })
    @PutMapping("/{id}/items")
    @Transactional
    public ResponseEntity<OrderResponse> modifyQuantityOrderProduct(
            @Parameter(description = "Unique identifier of the order", required = true) @PathVariable("id") Long orderId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product ID and new quantity",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateOrderItemRequest.class)))
            @Valid @RequestBody UpdateOrderItemRequest request) {
        Order updatedOrder = orderService.modifyQuantityProductInOrder(
                orderId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(orderMapper.fromOrderToResponse(updatedOrder));
    }

    @Operation(summary = "Update order status",
            description = "Updates the status of an existing order " +
                    "(e.g., from PENDING to APPROVED or REJECTED)." +
                    "\nRequires ADMIN OR CLIENT role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Unique identifier of the order", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New order status",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateOrderRequest.class)))
            @Valid @RequestBody UpdateOrderRequest request) {
        Order order = orderService.modifyOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(orderMapper.fromOrderToResponse(order));
    }

    @Operation(summary = "Remove product from order",
            description = "Removes a specific product from an existing order. " +
                    "Updates order total and restores product stock." +
                    "\nRequires ADMIN OR CLIENT role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product removed from order successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order or product not found", content = @Content)
    })
    @Transactional
    @DeleteMapping("/{order-id}/items/{product-id}")
    public ResponseEntity<OrderResponse> deleteItemFromOrder(
            @Parameter(description = "Unique identifier of the order", required = true) @PathVariable("order-id") Long orderId,
            @Parameter(description = "Unique identifier of the product to remove", required = true) @PathVariable("product-id") Long productId) {
        return ResponseEntity.ok(orderMapper.fromOrderToResponse(
                orderService.removeProductFromOrder(orderId, productId)));
    }
}