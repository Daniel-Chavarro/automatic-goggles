package org.java_avanzado.taller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.java_avanzado.events.EventTopology;
import org.java_avanzado.events.PaymentSucceededEvent;
import org.java_avanzado.taller.support.AbstractCommerceRabbitIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class CommerceRabbitTopologyTest extends AbstractCommerceRabbitIntegrationTest {

    @BeforeEach
    void setUp() {
        purgeQueue(EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE);
        purgeQueue(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ);
    }

    @Test
    void routesPaymentSuccessEventsThroughRabbitMqContainer() {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                42L,
                UUID.randomUUID(),
                BigDecimal.valueOf(19.99),
                UUID.randomUUID(),
                Instant.now());

        rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.PAYMENT_SUCCEEDED_V1, event);

        PaymentSucceededEvent received = awaitMessage(EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE,
                PaymentSucceededEvent.class);

        assertThat(received.orderId()).isEqualTo(event.orderId());
        assertThat(received.paymentId()).isEqualTo(event.paymentId());
        awaitQueueDepth(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ, 0);
    }
}
