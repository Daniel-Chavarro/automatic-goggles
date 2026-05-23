package org.java_avanzado.taller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.java_avanzado.events.EventTopology;
import org.java_avanzado.events.PaymentFailedEvent;
import org.java_avanzado.events.PaymentRequestedEvent;
import org.java_avanzado.payments.PaymentsServiceApplication;
import org.java_avanzado.payments.domain.PaymentStatus;
import org.java_avanzado.payments.persistence.entity.PaymentAttemptEntity;
import org.java_avanzado.payments.persistence.repository.PaymentAttemptRepository;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.RabbitMQContainer;

@Import(PaymentChoreographyIntegrationTest.ChoreographyObserverQueueConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentChoreographyIntegrationTest extends AbstractCommerceRabbitIntegrationTest {

    private static final Duration FLOW_TIMEOUT = Duration.ofSeconds(10);
    private static final BigDecimal SUCCESS_THRESHOLD = new BigDecimal("100.00");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RabbitMQContainer rabbitMqContainer;

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

    private MockMvc mockMvc;
    private ConfigurableApplicationContext paymentsContext;
    private PaymentAttemptRepository paymentAttemptRepository;

    @BeforeAll
    void startPaymentsApplication() {
        paymentsContext = new SpringApplicationBuilder(PaymentsServiceApplication.class)
                .run(
                        "--spring.main.web-application-type=none",
                        "--spring.datasource.url=jdbc:h2:mem:payments-choreography;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                        "--spring.datasource.username=sa",
                        "--spring.datasource.password=",
                        "--spring.datasource.driver-class-name=org.h2.Driver",
                        "--spring.jpa.hibernate.ddl-auto=create-drop",
                        "--spring.rabbitmq.dynamic=true",
                        "--spring.rabbitmq.listener.simple.auto-startup=true",
                        "--spring.rabbitmq.host=" + rabbitMqContainer.getHost(),
                        "--spring.rabbitmq.port=" + rabbitMqContainer.getAmqpPort(),
                        "--spring.rabbitmq.username=" + rabbitMqContainer.getAdminUsername(),
                        "--spring.rabbitmq.password=" + rabbitMqContainer.getAdminPassword(),
                        "--app.payments.max-success-amount=" + SUCCESS_THRESHOLD);
        paymentAttemptRepository = paymentsContext.getBean(PaymentAttemptRepository.class);
        RabbitAdmin paymentsRabbitAdmin = new RabbitAdmin(paymentsContext.getBean(ConnectionFactory.class));
        paymentsRabbitAdmin.setApplicationContext(paymentsContext);
        paymentsRabbitAdmin.initialize();
    }

    @AfterAll
    void stopPaymentsApplication() {
        if (paymentsContext != null) {
            paymentsContext.close();
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        paymentAttemptRepository.deleteAll();
        processedPaymentResultRepository.deleteAll();

        purgeQueue(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE);
        purgeQueue(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ);
        purgeQueue(EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE);
        purgeQueue(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ);
        purgeQueue(ChoreographyObserverQueueConfiguration.PAYMENT_REQUEST_OBSERVER_QUEUE);
        purgeQueue(ChoreographyObserverQueueConfiguration.PAYMENT_FAILED_OBSERVER_QUEUE);
    }

    @Test
    void checkoutBelowThresholdEventuallyApprovesOrder() throws Exception {
        ProductEntity product = createProduct("success-choreography-product", new BigDecimal("30.00"));
        Order order = createPendingOrderWithItem(product, 2);

        mockMvc.perform(post("/api/orders/{id}/checkout", order.getId())
                        .with(user("payment-client").roles("CLIENT")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value(order.getId()))
                .andExpect(jsonPath("$.status").value("PAYMENT_PENDING"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());

        PaymentRequestedEvent request = awaitMessage(
                ChoreographyObserverQueueConfiguration.PAYMENT_REQUEST_OBSERVER_QUEUE,
                PaymentRequestedEvent.class);

        Awaitility.await().atMost(FLOW_TIMEOUT).untilAsserted(() -> {
            assertThat(orderStatus(order.getId())).isEqualTo(OrderStatus.APPROVED);
            assertThat(paymentAttempt(request.correlationId()).getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        });

        rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.ORDER_PAYMENT_REQUESTED_V1, request);

        Awaitility.await().atMost(FLOW_TIMEOUT).untilAsserted(() -> {
            assertThat(paymentAttemptRepository.count()).isEqualTo(1);
            assertThat(orderStatus(order.getId())).isEqualTo(OrderStatus.APPROVED);
        });

        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ, 0);
        awaitQueueDepth(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ, 0);
    }

    @Test
    void checkoutAboveThresholdEventuallyRejectsAndRestocks() throws Exception {
        ProductEntity product = createProduct("failure-choreography-product", new BigDecimal("60.00"));
        int stockBeforeCheckout = product.getStockQuantity();
        Order order = createPendingOrderWithItem(product, 2);

        mockMvc.perform(post("/api/orders/{id}/checkout", order.getId())
                        .with(user("payment-client").roles("CLIENT")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value(order.getId()))
                .andExpect(jsonPath("$.status").value("PAYMENT_PENDING"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());

        PaymentFailedEvent failedEvent = awaitMessage(
                ChoreographyObserverQueueConfiguration.PAYMENT_FAILED_OBSERVER_QUEUE,
                PaymentFailedEvent.class);

        Awaitility.await().atMost(FLOW_TIMEOUT).untilAsserted(() -> {
            assertThat(orderStatus(order.getId())).isEqualTo(OrderStatus.REJECTED);
            assertThat(productStock(product.getId())).isEqualTo(stockBeforeCheckout);
            assertThat(paymentAttempt(failedEvent.correlationId()).getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(processedPaymentResultRepository.count()).isEqualTo(1);
        });

        rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.PAYMENT_FAILED_V1, failedEvent);

        Awaitility.await().atMost(FLOW_TIMEOUT).untilAsserted(() -> {
            assertThat(orderStatus(order.getId())).isEqualTo(OrderStatus.REJECTED);
            assertThat(productStock(product.getId())).isEqualTo(stockBeforeCheckout);
            assertThat(processedPaymentResultRepository.count()).isEqualTo(1);
        });

        awaitQueueDepth(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ, 0);
        awaitQueueDepth(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ, 0);
    }

    private PaymentAttemptEntity paymentAttempt(UUID correlationId) {
        return paymentAttemptRepository.findAll().stream()
                .filter(attempt -> correlationId.equals(attempt.getCorrelationId()))
                .findFirst()
                .orElseThrow();
    }

    private OrderStatus orderStatus(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow().getOrderStatus();
    }

    private int productStock(Long productId) {
        return productRepository.findById(productId).orElseThrow().getStockQuantity();
    }

    private Order createPendingOrderWithItem(ProductEntity product, int quantity) {
        Order order = orderService.createOrder(createUser().getId());
        return orderService.addProductToOrder(order.getId(), product.getId(), quantity);
    }

    private UserEntity createUser() {
        String suffix = UUID.randomUUID().toString();
        return userRepository.save(UserEntity.builder()
                .firstName("Payment")
                .lastName("Choreography")
                .email("payment-choreography-" + suffix + "@example.com")
                .password("password")
                .phone("1234567890")
                .role(UserRole.CLIENT)
                .active(true)
                .build());
    }

    private ProductEntity createProduct(String name, BigDecimal price) {
        return productRepository.save(ProductEntity.builder()
                .name(name + "-" + UUID.randomUUID())
                .description("Payment choreography test product")
                .price(price)
                .stockQuantity(10)
                .active(true)
                .build());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ChoreographyObserverQueueConfiguration {

        static final String PAYMENT_REQUEST_OBSERVER_QUEUE = "test.payment-request-observer";
        static final String PAYMENT_FAILED_OBSERVER_QUEUE = "test.payment-failed-observer";

        @Bean(name = PAYMENT_REQUEST_OBSERVER_QUEUE)
        Queue paymentRequestObserverQueue() {
            return QueueBuilder.durable(PAYMENT_REQUEST_OBSERVER_QUEUE).build();
        }

        @Bean(name = PAYMENT_FAILED_OBSERVER_QUEUE)
        Queue paymentFailedObserverQueue() {
            return QueueBuilder.durable(PAYMENT_FAILED_OBSERVER_QUEUE).build();
        }

        @Bean
        Binding paymentRequestObserverBinding(
                @Qualifier(PAYMENT_REQUEST_OBSERVER_QUEUE) Queue paymentRequestObserverQueue,
                @Qualifier("commerce.events") TopicExchange commerceEventsExchange) {
            return BindingBuilder.bind(paymentRequestObserverQueue)
                    .to(commerceEventsExchange)
                    .with(EventTopology.ORDER_PAYMENT_REQUESTED_V1);
        }

        @Bean
        Binding paymentFailedObserverBinding(
                @Qualifier(PAYMENT_FAILED_OBSERVER_QUEUE) Queue paymentFailedObserverQueue,
                @Qualifier("commerce.events") TopicExchange commerceEventsExchange) {
            return BindingBuilder.bind(paymentFailedObserverQueue)
                    .to(commerceEventsExchange)
                    .with(EventTopology.PAYMENT_FAILED_V1);
        }
    }
}
