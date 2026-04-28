package org.java_avanzado.taller.domain.model;

import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.exception.InsufficientStockException;
import org.java_avanzado.taller.exception.OrderAlreadyFinishedException;
import org.java_avanzado.taller.exception.ProductAlreadyInOrderException;
import org.java_avanzado.taller.exception.ProductNotFoundException;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    @Builder.Default
    private boolean active = true;

    /**
     * Timestamp when the order was created.
     */
    private Instant createdAt;

    /**
     * Timestamp of the last update to the order.
     */
    private Instant updatedAt;

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
            throw new OrderAlreadyFinishedException("Cannot update order status, order status is " + orderStatus);
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
            throw new OrderAlreadyFinishedException("Cannot update order status, order status is " + orderStatus);
        }

        if (orderProducts == null) {
            orderProducts = new HashSet<>();
        }

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        // Find the existing order product
        OrderProduct productToReStock = getOrderProductInSet(product.getId());
        orderProducts.remove(productToReStock);

        totalPrice = BigDecimal.valueOf(totalPrice.subtract(productToReStock.getUnitPrice().multiply(
                BigDecimal.valueOf(productToReStock.getQuantity()))).doubleValue());

        product.deductStock(-1*productToReStock.getQuantity());
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
            throw new OrderAlreadyFinishedException("Cannot update order status, order status is " + orderStatus);
        }

        if (orderProducts == null) {
            orderProducts = new HashSet<>();
        }

        if (product == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Product or quantity less than 0");
        }

        OrderProduct existingOrderProduct = getOrderProductInSet(product.getId());


        int quantityDifference = newQuantity - existingOrderProduct.getQuantity();

        if (quantityDifference > 0 && product.getQuantity() < quantityDifference) {
            throw new InsufficientStockException("Not enough stock for product: " + product.getName());
        }

        existingOrderProduct.setQuantity(newQuantity);
        totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(quantityDifference)));
        product.deductStock(quantityDifference);
    }

    /**
     * Modifies the order status to the specified new status, ensuring that the order is currently in a pending state.
     *
     * @param newStatus The new order status to be set.
     * @return A list of OrderProduct instances that need to be restocked if the new status is REJECTED, or null otherwise.
     */
    public List<OrderProduct> modifyOrderStatus(OrderStatus newStatus) {
        if (!this.orderStatus.equals(OrderStatus.PENDING)) {
            throw new OrderAlreadyFinishedException("Cannot update order status, order status is " + this.orderStatus);
        }

        List<OrderProduct> productsToRestock = null;

        if (newStatus == OrderStatus.REJECTED) {
            productsToRestock = new ArrayList<>(orderProducts);
        }

        this.setOrderStatus(newStatus);
        return productsToRestock;
    }

    /**
     * Deletes the order by marking it as inactive, ensuring that the order is currently in a pending state.
     *
     * @return A list of OrderProduct instances that need to be restocked when the order is deleted.
     * @throws OrderAlreadyFinishedException If the order is not in a pending state and cannot be deleted.
     */
    public List<OrderProduct> delete() {
        if (!this.orderStatus.equals(OrderStatus.PENDING)) {
            throw new OrderAlreadyFinishedException("Cannot delete order, order status is " + this.orderStatus);
        }

        List<OrderProduct> productsToRestock = new ArrayList<>(orderProducts);
        setActive(false);
        return productsToRestock;
    }

    /**
     * Helper method to find an OrderProduct in the orderProducts set by productId.
     *
     * @param productId The identifier of the product to find in the order.
     * @return The OrderProduct associated with the given productId.
     * @throws ProductNotFoundException If no OrderProduct with the specified productId is found
     */
    private OrderProduct getOrderProductInSet(Long productId) {
        return orderProducts.stream()
                .filter(op -> op.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product not found in order: " + id));
    }
}
