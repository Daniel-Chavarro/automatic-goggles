package org.java_avanzado.taller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
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
import org.java_avanzado.taller.service.PaymentResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.rabbitmq.dynamic=false",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class PaymentResultServiceConcurrencyTest {

    private static final int CONCURRENT_DELIVERIES = 8;
    private static final Duration ASSERTION_TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private PaymentResultService paymentResultService;

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
    void cleanProcessedEvents() {
        processedPaymentResultRepository.deleteAll();
    }

    @Test
    void concurrentDuplicateSuccessApprovesOrderOnce() throws Exception {
        ProductEntity product = createProduct("concurrent-success-product");
        Order order = createPaymentPendingOrder(product, 2);
        PaymentSucceededEvent event = succeededEvent(order);

        runConcurrently(() -> {
            paymentResultService.apply(event);
            return null;
        });

        assertThat(orderStatus(order.getId())).isEqualTo(OrderStatus.APPROVED);
        assertThat(processedPaymentResultRepository.count()).isEqualTo(1);
        assertThat(productStock(product.getId())).isEqualTo(8);
    }

    @Test
    void concurrentDuplicateFailureRejectsAndRestocksOnlyOnce() throws Exception {
        ProductEntity product = createProduct("concurrent-failure-product");
        Order order = createPaymentPendingOrder(product, 2);
        PaymentFailedEvent event = failedEvent(order);

        runConcurrently(() -> {
            paymentResultService.apply(event);
            return null;
        });

        Awaitility.await().atMost(ASSERTION_TIMEOUT).untilAsserted(() -> {
            assertThat(orderStatus(order.getId())).isEqualTo(OrderStatus.REJECTED);
            assertThat(productStock(product.getId())).isEqualTo(10);
            assertThat(processedPaymentResultRepository.count()).isEqualTo(1);
        });
    }

    private void runConcurrently(Callable<Void> callable) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_DELIVERIES);
        CountDownLatch ready = new CountDownLatch(CONCURRENT_DELIVERIES);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<Void>> futures = java.util.stream.IntStream.range(0, CONCURRENT_DELIVERIES)
                    .mapToObj(index -> executor.submit(() -> {
                        ready.countDown();
                        assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
                        return callable.call();
                    }))
                    .toList();

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            for (Future<Void> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }
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
                .lastName("Concurrent")
                .email("payment-concurrent-" + suffix + "@example.com")
                .password("password")
                .phone("1234567890")
                .role(UserRole.CLIENT)
                .active(true)
                .build());
    }

    private ProductEntity createProduct(String name) {
        return productRepository.save(ProductEntity.builder()
                .name(name + "-" + UUID.randomUUID())
                .description("Concurrent payment result test product")
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
