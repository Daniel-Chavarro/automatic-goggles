package org.java_avanzado.taller.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

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
    private String name;

    /**
     * Human-readable details about the product.
     */
    private String description;

    /**
     * Current sale price for one unit.
     */
    private BigDecimal price;

    /**
     * Quantity available in stock.
     */
    private int quantity;

    /**
     * Indicates whether the product is active and available.
     */
    private boolean active;

    /**
     * Optimistic locking version.
     */
    private BigInteger version;

    public void deductStock(int amount) {
        if (this.quantity < amount) {
            throw new org.java_avanzado.taller.domain.exception.InsufficientStockException("Not enough stock for product: " + this.name);
        }
        this.quantity -= amount;
    }
}
