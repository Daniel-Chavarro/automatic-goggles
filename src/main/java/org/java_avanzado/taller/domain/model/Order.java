package org.java_avanzado.taller.domain.model;

import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.exception.InsufficientStockException;
import org.java_avanzado.taller.domain.exception.OrderFinishedExeption;
import org.java_avanzado.taller.domain.exception.ProductAlreadyInOrderException;
import org.java_avanzado.taller.domain.exception.ProductNotFoundException;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Domain model representing a customer's order.
 */
@Builder
@Data
public class Order {
    /**
     * Internal order identifier.
     */
    private Long id;

    /**
     * Identifier of the user who owns the order.
     */
    private UUID userId;

    /**
     * Final total amount for the order.
     */
    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO;

    /**
     * Current workflow status of the order.
     */
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    /**
     * Collection of products included in the order.
     */
    @Builder.Default
    private Set<OrderProduct> orderProducts = new HashSet<>();

    /**
     * Indicates whether the order is active in the system.
     */
    private boolean active;

    /**
     * Adds a product to the order with the specified quantity, ensuring that the product has sufficient stock.
     *
     * @param product  The product to be added to the order.
     * @param quantity The quantity of the product to be added.
     * @throws IllegalArgumentException       If the product is null or the quantity is less than or equal to zero.
     * @throws InsufficientStockException     If the product does not have enough stock to fulfill the requested quantity.
     * @throws ProductAlreadyInOrderException If the product is already included in the order.
     */
    public void addProduct(Product product, int quantity) {
        if (!orderStatus.equals(OrderStatus.PENDING)) {
            throw new OrderFinishedExeption("Cannot update order status, order status is " + orderStatus);
        }

        if (orderProducts == null) {
            orderProducts = new HashSet<>();
        }

        if (product == null || quantity <= 0) {
            throw new IllegalArgumentException("Product or quantity less than 0");
        }

        if (product.getQuantity() < quantity) {
            throw new InsufficientStockException("Not enough stock for product: " + product.getName());
        }

        OrderProduct orderProduct = OrderProduct.builder()
                .productId(product.getId())
                .unitPrice(product.getPrice())
                .quantity(quantity)
                .build();

        if (!orderProducts.add(orderProduct)) {
            throw new ProductAlreadyInOrderException("Product already in order");
        }

        totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        product.deductStock(quantity);
    }

    /**
     * Removes a product from the order, ensuring that the product is currently included in the order.
     *
     * @param product The product to be removed from the order.
     * @throws IllegalArgumentException If the product is null.
     * @throws ProductNotFoundException If the product is not found in the order.
     */
    public void removeProduct(Product product) {
        if (!orderStatus.equals(OrderStatus.PENDING)) {
            throw new OrderFinishedExeption("Cannot update order status, order status is " + orderStatus);
        }

        if (orderProducts == null) {
            orderProducts = new HashSet<>();
        }

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        OrderProduct orderProduct = OrderProduct.builder()
                .productId(product.getId())
                .build();

        if (!orderProducts.contains(orderProduct)) {
            throw new ProductNotFoundException("Product not found in order: " + id);
        }

        orderProducts.remove(orderProduct);
    }

    /**
     * Updates the quantity of a product in the order, ensuring that the product is currently
     * included in the order and that there is sufficient stock available.
     *
     * @param product     The product for which to update the quantity.
     * @param newQuantity The new quantity for the product.
     * @throws IllegalArgumentException   If the product is null or the new quantity is less than or equal to zero.
     * @throws ProductNotFoundException   If the product is not found in the order.
     * @throws InsufficientStockException If there is not enough stock to fulfill the requested quantity.
     */
    public void updateQuantity(Product product, int newQuantity) {
        if (!orderStatus.equals(OrderStatus.PENDING)) {
            throw new OrderFinishedExeption("Cannot update order status, order status is " + orderStatus);
        }

        if (orderProducts == null) {
            orderProducts = new HashSet<>();
        }

        if (product == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Product or quantity less than 0");
        }

        OrderProduct orderProduct = OrderProduct.builder()
                .productId(product.getId())
                .build();

        if (!orderProducts.contains(orderProduct)) {
            throw new ProductNotFoundException("Product not found in order: " + id);
        }

        // Find the existing order product
        OrderProduct existingOrderProduct = orderProducts.stream()
                .filter(op -> op.getProductId().equals(product.getId()))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product not found in order: " + id));


        int quantityDifference = newQuantity - existingOrderProduct.getQuantity();

        if (quantityDifference > 0 && product.getQuantity() < quantityDifference) {
            throw new InsufficientStockException("Not enough stock for product: " + product.getName());
        }

        existingOrderProduct.setQuantity(newQuantity);
        totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(quantityDifference)));
        product.deductStock(quantityDifference);
    }
}
