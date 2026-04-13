package org.java_avanzado.taller_1_java.domain.model;

/**
 * Defines the lifecycle states available for an order.
 */
public enum OrderStatus {
    /**
     * Order was created and is waiting for review.
     */
    PENDING,

    /**
     * Order was accepted and can move forward for processing.
     */
    APPROVED,

    /**
     * Order was denied and should not be processed.
     */
    REJECTED,
}
