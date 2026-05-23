package org.java_avanzado.payments.service;

import java.time.Instant;
import java.util.UUID;
import org.java_avanzado.events.EventTopology;
import org.java_avanzado.events.PaymentFailedEvent;
import org.java_avanzado.events.PaymentRequestedEvent;
import org.java_avanzado.events.PaymentSucceededEvent;
import org.java_avanzado.payments.domain.PaymentStatus;
import org.java_avanzado.payments.persistence.entity.PaymentAttemptEntity;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for listening and processing payment requests via RabbitMQ.
 *
 * <p>This service consumes payment request events from the commerce service,
 * simulates payment processing, and publishes payment result events back
 * (either success or failure) for the commerce service to handle.</p>
 */
@Service
public class PaymentRequestListener {

    private static final int SCHEMA_VERSION = 1;

    private final PaymentSimulationService paymentSimulationService;
    private final RabbitTemplate rabbitTemplate;

    public PaymentRequestListener(PaymentSimulationService paymentSimulationService,
                                  RabbitTemplate rabbitTemplate) {
        this.paymentSimulationService = paymentSimulationService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(
            queues = EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE,
            containerFactory = "rabbitListenerContainerFactory")
    public void handle(PaymentRequestedEvent event) {
        PaymentAttemptEntity attempt = paymentSimulationService.simulate(event);
        publishResult(event, attempt);
    }

    /**
     * Publishes the result of a payment attempt back to the commerce service.
     *
     * <p>Determines whether the payment succeeded or failed and publishes the
     * appropriate event with the payment status and details.</p>
     *
     * @param request the original payment request event
     * @param attempt the payment attempt result
     * @throws IllegalStateException if the payment attempt status is not supported
     */
    private void publishResult(PaymentRequestedEvent request, PaymentAttemptEntity attempt) {
        if (attempt.getStatus() == PaymentStatus.SUCCEEDED) {
            rabbitTemplate.convertAndSend(
                    EventTopology.EXCHANGE,
                    EventTopology.PAYMENT_SUCCEEDED_V1,
                    succeededEvent(request, attempt));
            return;
        }

        if (attempt.getStatus() == PaymentStatus.FAILED) {
            rabbitTemplate.convertAndSend(
                    EventTopology.EXCHANGE,
                    EventTopology.PAYMENT_FAILED_V1,
                    failedEvent(request, attempt));
            return;
        }

        throw new IllegalStateException("Unsupported payment attempt status: " + attempt.getStatus());
    }

    /**
     * Creates a PaymentSucceededEvent from a successful payment attempt.
     *
     * @param request the original payment request
     * @param attempt the successful payment attempt
     * @return the payment succeeded event
     */
    private PaymentSucceededEvent succeededEvent(PaymentRequestedEvent request, PaymentAttemptEntity attempt) {
        return new PaymentSucceededEvent(
                UUID.randomUUID(),
                SCHEMA_VERSION,
                request.correlationId(),
                request.orderId(),
                request.userId(),
                request.amount(),
                attempt.getPaymentId(),
                Instant.now());
    }

    /**
     * Creates a PaymentFailedEvent from a failed payment attempt.
     *
     * @param request the original payment request
     * @param attempt the failed payment attempt
     * @return the payment failed event
     */
    private PaymentFailedEvent failedEvent(PaymentRequestedEvent request, PaymentAttemptEntity attempt) {
        return new PaymentFailedEvent(
                UUID.randomUUID(),
                SCHEMA_VERSION,
                request.correlationId(),
                request.orderId(),
                request.userId(),
                request.amount(),
                attempt.getPaymentId(),
                attempt.getFailureCode(),
                attempt.getFailureReason(),
                Instant.now());
    }
}
