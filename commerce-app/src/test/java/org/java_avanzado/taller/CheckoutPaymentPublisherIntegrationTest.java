package org.java_avanzado.taller;

import org.java_avanzado.events.EventTopology;
import org.java_avanzado.events.PaymentRequestedEvent;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.enums.UserRole;
import org.java_avanzado.taller.exception.OrderAlreadyFinishedException;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.service.OrderService;
import org.java_avanzado.taller.support.AbstractCommerceRabbitIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(CheckoutPaymentPublisherIntegrationTest.PaymentRequestQueueTestConfiguration.class)
class CheckoutPaymentPublisherIntegrationTest extends AbstractCommerceRabbitIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        purgeQueue(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE);
        purgeQueue(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ);
    }

    @Test
    void publishesPaymentRequestAfterCommit() {
        Order order = createPendingOrderWithItem(createProduct("payment-request-product"), 2);

        Order checkedOutOrder = orderService.checkoutOrder(order.getId());

        PaymentRequestedEvent event = awaitMessage(
                EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE,
                PaymentRequestedEvent.class);

        assertThat(event.eventId()).isNotNull();
        assertThat(event.schemaVersion()).isEqualTo(1);
        assertThat(event.correlationId()).isEqualTo(checkedOutOrder.getCheckoutCorrelationId());
        assertThat(event.orderId()).isEqualTo(checkedOutOrder.getId());
        assertThat(event.userId()).isEqualTo(checkedOutOrder.getUserId());
        assertThat(event.amount()).isEqualByComparingTo(checkedOutOrder.getTotalPrice());
        assertThat(event.occurredAt()).isNotNull();
        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE, 0);

        assertThatThrownBy(() -> orderService.checkoutOrder(order.getId()))
                .isInstanceOf(OrderAlreadyFinishedException.class)
                .hasMessage("Cannot checkout order, order status is PAYMENT_PENDING");
        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE, 0);
    }

    @Test
    void doesNotPublishWhenCheckoutRollsBack() {
        Order order = createPendingOrderWithItem(createProduct("rollback-payment-request-product"), 1);

        transactionTemplate.executeWithoutResult(status -> {
            orderService.checkoutOrder(order.getId());
            status.setRollbackOnly();
        });

        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE, 0);
    }

    private Order createPendingOrderWithItem(ProductEntity product, int quantity) {
        Order order = orderService.createOrder(createUser().getId());
        return orderService.addProductToOrder(order.getId(), product.getId(), quantity);
    }

    private UserEntity createUser() {
        String suffix = UUID.randomUUID().toString();
        return userRepository.save(UserEntity.builder()
                .firstName("Checkout")
                .lastName("Client")
                .email("checkout-payment-" + suffix + "@example.com")
                .password("password")
                .phone("1234567890")
                .role(UserRole.CLIENT)
                .active(true)
                .build());
    }

    private ProductEntity createProduct(String name) {
        return productRepository.save(ProductEntity.builder()
                .name(name + "-" + UUID.randomUUID())
                .description("Checkout payment publisher test product")
                .price(new BigDecimal("25.00"))
                .stockQuantity(10)
                .active(true)
                .build());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class PaymentRequestQueueTestConfiguration {

        @Bean(name = "payments.payment-requests")
        Queue paymentsPaymentRequestsQueue() {
            return QueueBuilder.durable(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE)
                    .deadLetterExchange(EventTopology.EXCHANGE)
                    .deadLetterRoutingKey(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ)
                    .build();
        }

        @Bean(name = "payments.payment-requests.dlq")
        Queue paymentsPaymentRequestsDlq() {
            return QueueBuilder.durable(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ).build();
        }

        @Bean
        Binding paymentRequestedBinding(
                @Qualifier("payments.payment-requests") Queue paymentsPaymentRequestsQueue,
                @Qualifier("commerce.events") TopicExchange commerceEventsExchange) {
            return BindingBuilder.bind(paymentsPaymentRequestsQueue)
                    .to(commerceEventsExchange)
                    .with(EventTopology.ORDER_PAYMENT_REQUESTED_V1);
        }

        @Bean
        Binding paymentsPaymentRequestsDlqBinding(
                @Qualifier("payments.payment-requests.dlq") Queue paymentsPaymentRequestsDlq,
                @Qualifier("commerce.events") TopicExchange commerceEventsExchange) {
            return BindingBuilder.bind(paymentsPaymentRequestsDlq)
                    .to(commerceEventsExchange)
                    .with(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ);
        }
    }
}
