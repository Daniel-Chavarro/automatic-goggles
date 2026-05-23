package org.java_avanzado.taller.service;

import org.java_avanzado.taller.domain.model.Order;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application event record indicating a payment request is ready to be published.
 *
 * <p>This record contains all the information needed to create a payment request event
 * for the payment service. It is published as an application event after checkout to
 * trigger asynchronous payment processing.</p>
 *
 * @param correlationId the correlation ID for tracking the payment in the system
 * @param orderId the ID of the order being paid for
 * @param userId the ID of the user placing the order
 * @param amount the total amount to be paid
 */
public record PaymentRequestReadyEvent(
        UUID correlationId,
        Long orderId,
        UUID userId,
        BigDecimal amount
) {

    static PaymentRequestReadyEvent from(Order order) {
        return new PaymentRequestReadyEvent(
                order.getCheckoutCorrelationId(),
                order.getId(),
                order.getUserId(),
                order.getTotalPrice());
    }
}
