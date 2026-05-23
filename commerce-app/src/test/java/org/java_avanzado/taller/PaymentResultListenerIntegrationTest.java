package org.java_avanzado.taller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.java_avanzado.events.EventTopology;
import org.java_avanzado.events.PaymentFailedEvent;
import org.java_avanzado.events.PaymentSucceededEvent;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;
import org.java_avanzado.taller.domain.model.enums.UserRole;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.OrderRepository;
import org.java_avanzado.taller.persistence.repository.ProcessedPaymentResultRepository;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.service.OrderService;
import org.java_avanzado.taller.support.AbstractCommerceRabbitIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

class PaymentResultListenerIntegrationTest extends AbstractCommerceRabbitIntegrationTest {

    private static final Duration RESULT_TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProcessedPaymentResultRepository processedPaymentResultRepository;

    @BeforeEach
    void setUp() {
        processedPaymentResultRepository.deleteAll();
        purgeQueue(EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE);
        purgeQueue(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ);
    }

    @Test
    void paymentSucceededApprovesPaymentPendingOrder() {
        Order order = createPaymentPendingOrder(createProduct("payment-success-product"), 2);
        PaymentSucceededEvent event = succeededEvent(order);

        rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.PAYMENT_SUCCEEDED_V1, event);

        Awaitility.await().atMost(RESULT_TIMEOUT).untilAsserted(() -> assertThat(orderStatus(order.getId()))
                .isEqualTo(OrderStatus.APPROVED));
        assertThat(processedPaymentResultRepository.existsById(event.eventId())).isTrue();
        awaitQueueDepth(EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE, 0);
        awaitQueueDepth(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ, 0);
    }

    @Test
    void duplicatePaymentFailedRejectsAndRestocksOnlyOnce() {
        ProductEntity product = createProduct("payment-failed-product");
        Order order = createPaymentPendingOrder(product, 2);
        PaymentFailedEvent event = failedEvent(order);

        rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.PAYMENT_FAILED_V1, event);
        Awaitility.await().atMost(RESULT_TIMEOUT).untilAsserted(() -> {
            assertThat(orderStatus(order.getId())).isEqualTo(OrderStatus.REJECTED);
            assertThat(productStock(product.getId())).isEqualTo(10);
        });

        rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.PAYMENT_FAILED_V1, event);
        Awaitility.await().atMost(RESULT_TIMEOUT).untilAsserted(() -> assertThat(queueDepth(
                EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE)).isZero());

        assertThat(productStock(product.getId())).isEqualTo(10);
        assertThat(processedPaymentResultRepository.count()).isEqualTo(1);
        awaitQueueDepth(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ, 0);
    }

    @Test
    void malformedPaymentResultRoutesToDlq() {
        Message malformed = MessageBuilder
                .withBody("{not-valid-json".getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("__TypeId__", PaymentSucceededEvent.class.getName())
                .build();

        rabbitTemplate.send(EventTopology.EXCHANGE, EventTopology.PAYMENT_SUCCEEDED_V1, malformed);

        awaitQueueDepth(EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE, 0);
        awaitQueueDepth(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ, 1);
        assertThat(processedPaymentResultRepository.count()).isZero();
    }

    private Order createPaymentPendingOrder(ProductEntity product, int quantity) {
        Order order = orderService.createOrder(createUser().getId());
        orderService.addProductToOrder(order.getId(), product.getId(), quantity);
        return orderService.checkoutOrder(order.getId());
    }

    private UserEntity createUser() {
        String suffix = UUID.randomUUID().toString();
        return userRepository.save(UserEntity.builder()
                .firstName("Payment")
                .lastName("Result")
                .email("payment-result-" + suffix + "@example.com")
                .password("password")
                .phone("1234567890")
                .role(UserRole.CLIENT)
                .active(true)
                .build());
    }

    private ProductEntity createProduct(String name) {
        return productRepository.save(ProductEntity.builder()
                .name(name + "-" + UUID.randomUUID())
                .description("Payment result listener test product")
                .price(new BigDecimal("25.00"))
                .stockQuantity(10)
                .active(true)
                .build());
    }

    private PaymentSucceededEvent succeededEvent(Order order) {
        return new PaymentSucceededEvent(
                UUID.randomUUID(),
                1,
                order.getCheckoutCorrelationId(),
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                UUID.randomUUID(),
                Instant.now());
    }

    private PaymentFailedEvent failedEvent(Order order) {
        return new PaymentFailedEvent(
                UUID.randomUUID(),
                1,
                order.getCheckoutCorrelationId(),
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                UUID.randomUUID(),
                "PAYMENT_DECLINED",
                "Payment was declined",
                Instant.now());
    }

    private OrderStatus orderStatus(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow().getOrderStatus();
    }

    private int productStock(Long productId) {
        return productRepository.findById(productId).orElseThrow().getStockQuantity();
    }
}
