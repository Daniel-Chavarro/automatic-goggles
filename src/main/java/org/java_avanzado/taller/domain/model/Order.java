package org.java_avanzado.taller.domain.model;

import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
    private BigDecimal totalPrice;

    /**
     * Current workflow status of the order.
     */
    private OrderStatus orderStatus;

    /**
     * Collection of products included in the order.
     */
    @Builder.Default
    private List<OrderProduct> orderProducts = new ArrayList<>();

    /**
     * Indicates whether the order is active in the system.
     */
    private boolean active;

}
