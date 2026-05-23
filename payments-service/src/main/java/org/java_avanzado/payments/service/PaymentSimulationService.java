package org.java_avanzado.payments.service;

import java.math.BigDecimal;
import java.util.UUID;
import org.java_avanzado.events.PaymentRequestedEvent;
import org.java_avanzado.payments.domain.PaymentStatus;
import org.java_avanzado.payments.persistence.entity.PaymentAttemptEntity;
import org.java_avanzado.payments.persistence.repository.PaymentAttemptRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Service for simulating payment processing.
 *
 * <p>This service simulates payment processing by determining whether a payment
 * succeeds or fails based on the amount and configured threshold. It ensures
 * idempotency by detecting duplicate payment attempts and reusing existing results.</p>
 */
@Service
public class PaymentSimulationService {

    /**
     * Failure code for payments exceeding the configured threshold.
     */
    public static final String THRESHOLD_EXCEEDED_CODE = "PAYMENT_THRESHOLD_EXCEEDED";
    /**
     * Failure reason message for payments exceeding the configured threshold.
     */
    public static final String THRESHOLD_EXCEEDED_REASON = "Payment amount exceeds the configured success threshold";

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentAttemptIdempotencyService paymentAttemptIdempotencyService;
    private final PaymentSimulationProperties properties;
    private final Object paymentAttemptCreationLock = new Object();

    public PaymentSimulationService(PaymentAttemptRepository paymentAttemptRepository,
                                    PaymentAttemptIdempotencyService paymentAttemptIdempotencyService,
                                    PaymentSimulationProperties properties) {
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.paymentAttemptIdempotencyService = paymentAttemptIdempotencyService;
        this.properties = properties;
    }

    /**
     * Simulates payment processing for the given payment request event.
     *
     * <p>If a payment attempt already exists for this event (detected by event ID
     * or correlation ID), returns the existing result for idempotency. Otherwise,
     * creates a new payment attempt with either success or failure status based on
     * the payment amount.</p>
     *
     * @param event the payment request event to process
     * @return the payment attempt entity with the simulation result
     */
    public PaymentAttemptEntity simulate(PaymentRequestedEvent event) {
        return paymentAttemptRepository.findBySourceEventIdOrCorrelationId(event.eventId(), event.correlationId())
                .orElseGet(() -> saveOrReuseExistingWithLocalSerialization(event));
    }

    private PaymentAttemptEntity saveOrReuseExistingWithLocalSerialization(PaymentRequestedEvent event) {
        synchronized (paymentAttemptCreationLock) {
            return paymentAttemptRepository.findBySourceEventIdOrCorrelationId(event.eventId(), event.correlationId())
                    .orElseGet(() -> saveOrReuseExisting(event));
        }
    }

    /**
     * Saves a new payment attempt or reuses an existing one if a duplicate key conflict occurs.
     *
     * <p>Attempts to save the new payment attempt using idempotency service. If a duplicate
     * key exception occurs, re-queries the database to find the existing payment attempt
     * that was created, ensuring idempotent behavior.</p>
     *
     * @param event the payment request event
     * @return the saved or existing payment attempt entity
     */
    private PaymentAttemptEntity saveOrReuseExisting(PaymentRequestedEvent event) {
        try {
            return paymentAttemptIdempotencyService.saveNew(newPaymentAttempt(event));
        } catch (DataIntegrityViolationException exception) {
            return paymentAttemptIdempotencyService
                    .findAfterDuplicateKey(event.eventId(), event.correlationId(), exception)
                    .orElseThrow();
        }
    }

    /**
     * Creates a new payment attempt entity based on the payment request event.
     *
     * <p>Determines the payment status by comparing the amount against the configured
     * threshold. Payments up to the threshold succeed, others fail with the appropriate
     * failure code and reason.</p>
     *
     * @param event the payment request event containing the payment details
     * @return a new PaymentAttemptEntity with success/failure status
     */
    private PaymentAttemptEntity newPaymentAttempt(PaymentRequestedEvent event) {
        boolean succeeds = event.amount().compareTo(maxSuccessAmount()) <= 0;
        PaymentStatus status = succeeds ? PaymentStatus.SUCCEEDED : PaymentStatus.FAILED;
        String failureCode = succeeds ? null : THRESHOLD_EXCEEDED_CODE;
        String failureReason = succeeds ? null : THRESHOLD_EXCEEDED_REASON;
        return new PaymentAttemptEntity(
                UUID.randomUUID(),
                event.orderId(),
                event.correlationId(),
                event.eventId(),
                event.amount(),
                status,
                failureCode,
                failureReason);
    }

    private BigDecimal maxSuccessAmount() {
        return properties.maxSuccessAmount();
    }
}
