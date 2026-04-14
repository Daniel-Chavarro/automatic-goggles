package org.java_avanzado.taller.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Domain model representing a product line within an order.
 */
@Data
@Builder
public class OrderProduct {
    /**
     * Internal identifier for the order line item.
     */
    private Long id;

    /**
     * Identifier of the referenced product.
     */
    private String productId;

    /**
     * Number of units requested for the product.
     */
    private Integer quantity;

    /**
     * Product unit price captured at purchase time.
     */
    private BigDecimal unitPrice;
}
