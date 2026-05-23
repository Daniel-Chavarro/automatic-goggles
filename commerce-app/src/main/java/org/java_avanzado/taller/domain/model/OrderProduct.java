package org.java_avanzado.taller.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    @EqualsAndHashCode.Exclude
    private Long id;

    /**
     * Identifier of the referenced product.
     */
    private Long productId;

    /**
     * Identifier of the parent order to which this line item belongs.
     */
    @EqualsAndHashCode.Exclude
    private Long orderId;

    /**
     * Number of units requested for the product.
     */
    @EqualsAndHashCode.Exclude
    private Integer quantity;

    /**
     * Product unit price captured at purchase time.
     */
    @EqualsAndHashCode.Exclude
    private BigDecimal unitPrice;
}
