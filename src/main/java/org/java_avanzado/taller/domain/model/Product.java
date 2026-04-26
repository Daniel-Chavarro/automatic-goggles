package org.java_avanzado.taller.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import org.java_avanzado.taller.domain.exception.InsufficientStockException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

/**
 * Domain model representing a catalog product.
 */
@Data
@Builder
public class Product {
    /**
     * Internal product identifier.
     */
    private Long id;

    /**
     * Display name of the product.
     */
    @NotBlank
    private String name;

    /**
     * Human-readable details about the product.
     */
    private String description;

    /**
     * Current sale price for one unit.
     */
    @Positive
    private BigDecimal price;

    /**
     * Quantity available in stock.
     */
    @Positive
    private int quantity;

    /**
     * Indicates whether the product is active and available.
     */
    @Builder.Default
    private boolean active = true;

    /**
     * Optimistic locking version.
     */
    private BigInteger version;

    /**
     * Timestamp when the product was created.
     */
    private Instant createdAt;

    /**
     * Timestamp of the last update to the product.
     */
    private Instant updatedAt;

    /**
     * Deducts the specified amount from the product's stock quantity, ensuring that the resulting stock does not become negative.
     * <p>
     * Note: If the amount is negative, the result will be an increase in stock.
     *
     * @param amount The amount to deduct from the stock.
     */
    public void deductStock(int amount) {
        if (this.quantity < amount) {
            throw new InsufficientStockException("Not enough stock for product: " + this.name);
        }
        this.quantity -= amount;
    }
}
