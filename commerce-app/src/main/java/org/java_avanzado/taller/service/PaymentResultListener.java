package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.events.EventTopology;
import org.java_avanzado.events.PaymentFailedEvent;
import org.java_avanzado.events.PaymentSucceededEvent;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Service for listening and processing payment result events from RabbitMQ.
 *
 * <p>Receives payment result events (success or failure) from the payment service
 * and delegates processing to the PaymentResultService to update order statuses.</p>
 */
@Service
@RequiredArgsConstructor
@RabbitListener(
        queues = EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE,
        containerFactory = "rabbitListenerContainerFactory")
public class PaymentResultListener {

    private final PaymentResultService paymentResultService;

    /**
     * Handles successful payment events.
     *
     * @param event the succeeded payment event
     */
    @RabbitHandler
    public void handle(PaymentSucceededEvent event) {
        paymentResultService.apply(event);
    }

    /**
     * Handles failed payment events.
     *
     * @param event the failed payment event
     */
    @RabbitHandler
    public void handle(PaymentFailedEvent event) {
        paymentResultService.apply(event);
    }
}
