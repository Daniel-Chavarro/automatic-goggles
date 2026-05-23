package org.java_avanzado.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event record representing a failed payment from the payment service.
 *
 * <p>This event is published when a payment request has failed to process.
 * It contains failure information including the failure code and reason,
 * which can be used to provide feedback to the user and trigger retry logic.</p>
 *
 * @param eventId the unique identifier of this event
 * @param schemaVersion the version of the event schema
 * @param correlationId the correlation ID to track this payment through the system
 * @param orderId the order ID that failed to be paid
 * @param userId the user ID that placed the order
 * @param amount the payment amount that was attempted
 * @param paymentId the payment ID assigned by the payment service
 * @param failureCode the code indicating the type of failure
 * @param failureReason the human-readable reason for the failure
 * @param occurredAt the timestamp when the payment failed
 */
public record PaymentFailedEvent(
        UUID eventId,
        int schemaVersion,
        UUID correlationId,
        Long orderId,
        UUID userId,
        BigDecimal amount,
        UUID paymentId,
        String failureCode,
        String failureReason,
        Instant occurredAt
) {
}
