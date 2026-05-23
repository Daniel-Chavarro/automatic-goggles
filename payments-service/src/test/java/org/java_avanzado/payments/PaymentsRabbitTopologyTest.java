package org.java_avanzado.payments;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.java_avanzado.events.EventTopology;
import org.java_avanzado.events.PaymentRequestedEvent;
import org.java_avanzado.payments.support.AbstractPaymentsRabbitIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class PaymentsRabbitTopologyTest extends AbstractPaymentsRabbitIntegrationTest {

    @BeforeEach
    void setUp() {
        purgeQueue(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE);
        purgeQueue(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ);
    }

    @Test
    void routesPaymentRequestsThroughRabbitMqContainer() {
        PaymentRequestedEvent event = new PaymentRequestedEvent(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                42L,
                UUID.randomUUID(),
                BigDecimal.valueOf(19.99),
                Instant.now());

        rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.ORDER_PAYMENT_REQUESTED_V1, event);

        PaymentRequestedEvent received = awaitMessage(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE,
                PaymentRequestedEvent.class);

        assertThat(received.orderId()).isEqualTo(event.orderId());
        assertThat(received.amount()).isEqualByComparingTo(event.amount());
        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ, 0);
    }
}
