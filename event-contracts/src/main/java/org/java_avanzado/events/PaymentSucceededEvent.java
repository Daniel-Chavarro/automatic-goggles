package org.java_avanzado.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event record representing a successful payment from the payment service.
 *
 * <p>This event is published when a payment request has been successfully processed.
 * It contains the payment ID and other information needed to update the order status
 * and trigger subsequent workflows.</p>
 *
 * @param eventId the unique identifier of this event
 * @param schemaVersion the version of the event schema
 * @param correlationId the correlation ID to track this payment through the system
 * @param orderId the order ID that was paid
 * @param userId the user ID that placed the order
 * @param amount the payment amount
 * @param paymentId the payment ID assigned by the payment service
 * @param occurredAt the timestamp when the payment was processed
 */
public record PaymentSucceededEvent(
        UUID eventId,
        int schemaVersion,
        UUID correlationId,
        Long orderId,
        UUID userId,
        BigDecimal amount,
        UUID paymentId,
        Instant occurredAt
) {
}
