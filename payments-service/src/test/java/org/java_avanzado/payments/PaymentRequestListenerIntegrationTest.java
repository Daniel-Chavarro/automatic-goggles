package org.java_avanzado.payments;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.java_avanzado.events.EventTopology;
import org.java_avanzado.events.PaymentFailedEvent;
import org.java_avanzado.events.PaymentRequestedEvent;
import org.java_avanzado.events.PaymentSucceededEvent;
import org.java_avanzado.payments.persistence.repository.PaymentAttemptRepository;
import org.java_avanzado.payments.service.PaymentSimulationService;
import org.java_avanzado.payments.support.AbstractPaymentsRabbitIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import(PaymentRequestListenerIntegrationTest.PaymentResultQueueTestConfiguration.class)
class PaymentRequestListenerIntegrationTest extends AbstractPaymentsRabbitIntegrationTest {

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @BeforeEach
    void setUp() {
        paymentAttemptRepository.deleteAll();
        purgeQueue(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE);
        purgeQueue(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ);
        purgeQueue(EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE);
        purgeQueue(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ);
    }

    @Test
    void successfulPaymentPublishesSucceededEvent() {
        PaymentRequestedEvent request = paymentRequest(BigDecimal.valueOf(75.00));

        rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.ORDER_PAYMENT_REQUESTED_V1, request);

        PaymentSucceededEvent result = awaitMessage(
                EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE,
                PaymentSucceededEvent.class);

        assertThat(result.eventId()).isNotNull();
        assertThat(result.schemaVersion()).isEqualTo(1);
        assertThat(result.correlationId()).isEqualTo(request.correlationId());
        assertThat(result.orderId()).isEqualTo(request.orderId());
        assertThat(result.userId()).isEqualTo(request.userId());
        assertThat(result.amount()).isEqualByComparingTo(request.amount());
        assertThat(result.paymentId()).isNotNull();
        assertThat(result.occurredAt()).isNotNull();
        assertThat(paymentAttemptRepository.count()).isEqualTo(1);
        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE, 0);
        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ, 0);
    }

    @Test
    void failedPaymentPublishesFailedEventWithStableFailureDetails() {
        PaymentRequestedEvent request = paymentRequest(BigDecimal.valueOf(125.00));

        rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.ORDER_PAYMENT_REQUESTED_V1, request);

        PaymentFailedEvent result = awaitMessage(
                EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE,
                PaymentFailedEvent.class);

        assertThat(result.eventId()).isNotNull();
        assertThat(result.schemaVersion()).isEqualTo(1);
        assertThat(result.correlationId()).isEqualTo(request.correlationId());
        assertThat(result.orderId()).isEqualTo(request.orderId());
        assertThat(result.userId()).isEqualTo(request.userId());
        assertThat(result.amount()).isEqualByComparingTo(request.amount());
        assertThat(result.paymentId()).isNotNull();
        assertThat(result.failureCode()).isEqualTo(PaymentSimulationService.THRESHOLD_EXCEEDED_CODE);
        assertThat(result.failureReason()).isEqualTo(PaymentSimulationService.THRESHOLD_EXCEEDED_REASON);
        assertThat(result.occurredAt()).isNotNull();
        assertThat(paymentAttemptRepository.count()).isEqualTo(1);
        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ, 0);
    }

    @Test
    void duplicatePaymentRequestReusesPersistedAttempt() {
        PaymentRequestedEvent request = paymentRequest(BigDecimal.valueOf(75.00));

        rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.ORDER_PAYMENT_REQUESTED_V1, request);
        PaymentSucceededEvent firstResult = awaitMessage(
                EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE,
                PaymentSucceededEvent.class);

        rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.ORDER_PAYMENT_REQUESTED_V1, request);
        PaymentSucceededEvent duplicateResult = awaitMessage(
                EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE,
                PaymentSucceededEvent.class);

        assertThat(duplicateResult.paymentId()).isEqualTo(firstResult.paymentId());
        assertThat(duplicateResult.correlationId()).isEqualTo(firstResult.correlationId());
        assertThat(paymentAttemptRepository.count()).isEqualTo(1);
        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ, 0);
    }

    @Test
    void malformedPaymentRequestRoutesToDlq() {
        Message malformed = MessageBuilder
                .withBody("{not-valid-json".getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("__TypeId__", PaymentRequestedEvent.class.getName())
                .build();

        rabbitTemplate.send(EventTopology.EXCHANGE, EventTopology.ORDER_PAYMENT_REQUESTED_V1, malformed);

        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE, 0);
        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ, 1);
        assertThat(paymentAttemptRepository.count()).isZero();
    }

    private PaymentRequestedEvent paymentRequest(BigDecimal amount) {
        return new PaymentRequestedEvent(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                42L,
                UUID.randomUUID(),
                amount,
                Instant.now());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class PaymentResultQueueTestConfiguration {

        @Bean(name = "commerce.payment-results")
        Queue commercePaymentResultsQueue() {
            return QueueBuilder.durable(EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE)
                    .deadLetterExchange(EventTopology.EXCHANGE)
                    .deadLetterRoutingKey(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ)
                    .build();
        }

        @Bean(name = "commerce.payment-results.dlq")
        Queue commercePaymentResultsDlq() {
            return QueueBuilder.durable(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ).build();
        }

        @Bean
        Binding paymentSucceededBinding(
                @Qualifier("commerce.payment-results") Queue commercePaymentResultsQueue,
                @Qualifier("commerce.events") TopicExchange commerceEventsExchange) {
            return BindingBuilder.bind(commercePaymentResultsQueue)
                    .to(commerceEventsExchange)
                    .with(EventTopology.PAYMENT_SUCCEEDED_V1);
        }

        @Bean
        Binding paymentFailedBinding(
                @Qualifier("commerce.payment-results") Queue commercePaymentResultsQueue,
                @Qualifier("commerce.events") TopicExchange commerceEventsExchange) {
            return BindingBuilder.bind(commercePaymentResultsQueue)
                    .to(commerceEventsExchange)
                    .with(EventTopology.PAYMENT_FAILED_V1);
        }

        @Bean
        Binding commercePaymentResultsDlqBinding(
                @Qualifier("commerce.payment-results.dlq") Queue commercePaymentResultsDlq,
                @Qualifier("commerce.events") TopicExchange commerceEventsExchange) {
            return BindingBuilder.bind(commercePaymentResultsDlq)
                    .to(commerceEventsExchange)
                    .with(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ);
        }
    }
}
