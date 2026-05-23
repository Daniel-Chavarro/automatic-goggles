package org.java_avanzado.payments.domain;

/**
 * Enumeration of possible payment statuses.
 *
 * <p>Represents the lifecycle of a payment attempt through the payment service.</p>
 */
public enum PaymentStatus {
    /**
     * Payment is waiting to be processed.
     */
    PENDING,
    /**
     * Payment has been successfully processed.
     */
    SUCCEEDED,
    /**
     * Payment processing has failed.
     */
    FAILED
}
