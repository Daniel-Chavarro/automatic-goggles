package org.java_avanzado.taller.domain.model.enums;

import lombok.Getter;

/**
 * Defines the lifecycle states available for an order.
 */
@Getter
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
    REJECTED
}