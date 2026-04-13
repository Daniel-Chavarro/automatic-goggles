package org.java_avanzado.taller_1_java.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

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
}
