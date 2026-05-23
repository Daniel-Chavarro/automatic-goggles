package org.java_avanzado.payments;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.java_avanzado.events.PaymentRequestedEvent;
import org.java_avanzado.payments.domain.PaymentStatus;
import org.java_avanzado.payments.persistence.entity.PaymentAttemptEntity;
import org.java_avanzado.payments.persistence.repository.PaymentAttemptRepository;
import org.java_avanzado.payments.service.PaymentSimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.rabbitmq.dynamic=false",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class PaymentSimulationServiceTest {

    @Autowired
    private PaymentSimulationService paymentSimulationService;

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentAttemptRepository.deleteAll();
    }

    @Test
    void amountThresholdRulesAreDeterministic() {
        PaymentAttemptEntity belowThreshold = paymentSimulationService.simulate(paymentRequest(BigDecimal.valueOf(99.99)));
        PaymentAttemptEntity atThreshold = paymentSimulationService.simulate(paymentRequest(BigDecimal.valueOf(100.00)));
        PaymentAttemptEntity aboveThreshold = paymentSimulationService.simulate(paymentRequest(BigDecimal.valueOf(100.01)));

        assertThat(belowThreshold.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(belowThreshold.getFailureCode()).isNull();
        assertThat(belowThreshold.getPaymentId()).isNotNull();
        assertThat(atThreshold.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(atThreshold.getFailureCode()).isNull();
        assertThat(aboveThreshold.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(aboveThreshold.getFailureCode())
                .isEqualTo(PaymentSimulationService.THRESHOLD_EXCEEDED_CODE);
        assertThat(aboveThreshold.getFailureReason())
                .isEqualTo(PaymentSimulationService.THRESHOLD_EXCEEDED_REASON);
    }

    @Test
    void duplicateRequestDoesNotCreateSecondPayment() {
        UUID sourceEventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        PaymentRequestedEvent firstRequest = paymentRequest(sourceEventId, correlationId, BigDecimal.valueOf(125.00));
        PaymentRequestedEvent duplicateByEvent = paymentRequest(sourceEventId, UUID.randomUUID(), BigDecimal.valueOf(10.00));
        PaymentRequestedEvent duplicateByCorrelation = paymentRequest(UUID.randomUUID(), correlationId, BigDecimal.valueOf(10.00));

        PaymentAttemptEntity firstAttempt = paymentSimulationService.simulate(firstRequest);
        PaymentAttemptEntity eventDuplicateAttempt = paymentSimulationService.simulate(duplicateByEvent);
        PaymentAttemptEntity correlationDuplicateAttempt = paymentSimulationService.simulate(duplicateByCorrelation);

        assertThat(eventDuplicateAttempt.getPaymentId()).isEqualTo(firstAttempt.getPaymentId());
        assertThat(correlationDuplicateAttempt.getPaymentId()).isEqualTo(firstAttempt.getPaymentId());
        assertThat(eventDuplicateAttempt.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(correlationDuplicateAttempt.getFailureCode())
                .isEqualTo(PaymentSimulationService.THRESHOLD_EXCEEDED_CODE);
        assertThat(paymentAttemptRepository.count()).isEqualTo(1);
    }

    @Test
    void concurrentDuplicateRequestsReuseOnePaymentAttempt() throws Exception {
        UUID sourceEventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        PaymentRequestedEvent request = paymentRequest(sourceEventId, correlationId, BigDecimal.valueOf(125.00));
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch ready = new CountDownLatch(8);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Callable<PaymentAttemptEntity>> calls = java.util.stream.IntStream.range(0, 8)
                    .mapToObj(index -> (Callable<PaymentAttemptEntity>) () -> {
                        ready.countDown();
                        assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
                        return paymentSimulationService.simulate(request);
                    })
                    .toList();

            List<Future<PaymentAttemptEntity>> results = calls.stream()
                    .map(executor::submit)
                    .toList();
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<PaymentAttemptEntity> attempts = results.stream()
                    .map(PaymentSimulationServiceTest::getFuture)
                    .toList();

            assertThat(attempts)
                    .extracting(PaymentAttemptEntity::getPaymentId)
                    .containsOnly(attempts.getFirst().getPaymentId());
            assertThat(attempts)
                    .extracting(PaymentAttemptEntity::getStatus)
                    .containsOnly(PaymentStatus.FAILED);
            assertThat(paymentAttemptRepository.count()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private static <T> T getFuture(Future<T> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private PaymentRequestedEvent paymentRequest(BigDecimal amount) {
        return paymentRequest(UUID.randomUUID(), UUID.randomUUID(), amount);
    }

    private PaymentRequestedEvent paymentRequest(UUID sourceEventId, UUID correlationId, BigDecimal amount) {
        return new PaymentRequestedEvent(
                sourceEventId,
                1,
                correlationId,
                42L,
                UUID.randomUUID(),
                amount,
                Instant.now());
    }
}
