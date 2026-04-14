package org.java_avanzado.taller.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines the lifecycle states available for an order.
 */
@RequiredArgsConstructor
@Getter
public enum OrderStatus {
    /**
     * Order was created and is waiting for review.
     */
    PENDING("PENDING"),

    /**
     * Order was accepted and can move forward for processing.
     */
    APPROVED("APPROVED"),

    /**
     * Order was denied and should not be processed.
     */
    REJECTED("REJECTED");

    private final String value;
}
