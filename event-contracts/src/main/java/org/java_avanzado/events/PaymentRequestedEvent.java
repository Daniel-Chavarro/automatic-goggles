package org.java_avanzado.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event record representing a payment request from the commerce system.
 *
 * <p>This event is published when an order is ready for payment processing.
 * It contains all necessary information for the payment service to process
 * the payment and track results.</p>
 *
 * @param eventId the unique identifier of this event
 * @param schemaVersion the version of the event schema
 * @param correlationId the correlation ID to track this payment through the system
 * @param orderId the order ID associated with this payment
 * @param userId the user ID placing the order
 * @param amount the payment amount
 * @param occurredAt the timestamp when the event occurred
 */
public record PaymentRequestedEvent(
        UUID eventId,
        int schemaVersion,
        UUID correlationId,
        Long orderId,
        UUID userId,
        BigDecimal amount,
        Instant occurredAt
) {
}
