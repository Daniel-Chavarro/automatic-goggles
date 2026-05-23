package org.java_avanzado.taller.service;

import org.java_avanzado.events.EventTopology;
import org.java_avanzado.events.PaymentRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Component for publishing payment request events to RabbitMQ.
 *
 * <p>Listens for payment request ready events and publishes payment request
 * events to the payments service via RabbitMQ. Uses transactional event listeners
 * to ensure events are published only after the transaction commits.</p>
 */
@Component
public class PaymentRequestPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRequestPublisher.class);
    private static final int SCHEMA_VERSION = 1;

    private final RabbitTemplate rabbitTemplate;

    public PaymentRequestPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes a payment request event after the transaction commits.
     *
     * <p>Triggered when a PaymentRequestReadyEvent is published, converts it to a
     * PaymentRequestedEvent and sends it to the payments service via RabbitMQ.
     * Logs errors if publishing fails.</p>
     *
     * @param event the payment request ready event to publish
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishPaymentRequest(PaymentRequestReadyEvent event) {
        PaymentRequestedEvent paymentRequestedEvent = new PaymentRequestedEvent(
                UUID.randomUUID(),
                SCHEMA_VERSION,
                event.correlationId(),
                event.orderId(),
                event.userId(),
                event.amount(),
                Instant.now());

        try {
            rabbitTemplate.convertAndSend(
                    EventTopology.EXCHANGE,
                    EventTopology.ORDER_PAYMENT_REQUESTED_V1,
                    paymentRequestedEvent);
        } catch (AmqpException exception) {
            logger.error("Failed to publish payment request for order {} with correlation {}",
                    event.orderId(), event.correlationId(), exception);
        }
    }
}
