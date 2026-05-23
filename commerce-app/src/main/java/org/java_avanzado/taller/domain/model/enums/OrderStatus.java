package org.java_avanzado.taller.domain.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Defines the lifecycle states available for an order.
 */
@Getter
@Schema(description = "Order lifecycle states",
        allowableValues = {"PENDING", "PAYMENT_PENDING", "APPROVED", "REJECTED"})
public enum OrderStatus {

    /**
     * Order was created and is waiting for review.
     */
    @Schema(description = "Order was created and is waiting for review")
    PENDING,

    /**
     * Order checkout was finalized and payment is pending asynchronously.
     */
    @Schema(description = "Order checkout was finalized and payment is pending asynchronously")
    PAYMENT_PENDING,

    /**
     * Order was accepted and can move forward for processing.
     */
    @Schema(description = "Order was accepted and can move forward for processing")
    APPROVED,

    /**
     * Order was denied and should not be processed.
     */
    @Schema(description = "Order was denied and should not be processed")
    REJECTED
}
