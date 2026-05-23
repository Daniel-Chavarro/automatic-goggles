package org.java_avanzado.payments.service;

import java.util.Optional;
import java.util.UUID;
import org.java_avanzado.payments.persistence.entity.PaymentAttemptEntity;
import org.java_avanzado.payments.persistence.repository.PaymentAttemptRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing payment attempt idempotency.
 *
 * <p>Ensures that payment processing is idempotent by detecting duplicate payments
 * and managing their persistence with appropriate transaction propagation to handle
 * concurrent duplicate requests correctly.</p>
 */
@Service
public class PaymentAttemptIdempotencyService {

    private final PaymentAttemptRepository paymentAttemptRepository;

    public PaymentAttemptIdempotencyService(PaymentAttemptRepository paymentAttemptRepository) {
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    /**
     * Finds an existing payment attempt by source event ID or correlation ID.
     *
     * @param sourceEventId the source event ID of the payment request
     * @param correlationId the correlation ID of the payment
     * @return an Optional containing the payment attempt if found
     */
    @Transactional(readOnly = true)
    public Optional<PaymentAttemptEntity> findExisting(UUID sourceEventId, UUID correlationId) {
        return paymentAttemptRepository.findBySourceEventIdOrCorrelationId(sourceEventId, correlationId);
    }

    /**
     * Saves a new payment attempt with a new transaction.
     *
     * <p>Uses REQUIRES_NEW propagation to ensure this save operation runs in a new
     * transaction, allowing the caller to handle duplicate key constraints independently.</p>
     *
     * @param paymentAttempt the payment attempt entity to save
     * @return the saved payment attempt entity
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentAttemptEntity saveNew(PaymentAttemptEntity paymentAttempt) {
        return paymentAttemptRepository.saveAndFlush(paymentAttempt);
    }

    /**
     * Attempts to find a payment attempt after a duplicate key constraint violation.
     *
     * <p>Returns an existing payment attempt if found, otherwise re-throws the
     * DataIntegrityViolationException. This is used when a save operation fails
     * due to a duplicate key constraint, indicating another request already
     * created the payment attempt.</p>
     *
     * @param sourceEventId the source event ID of the payment request
     * @param correlationId the correlation ID of the payment
     * @param exception the duplicate key constraint exception
     * @return an Optional containing the existing payment attempt
     * @throws DataIntegrityViolationException if no payment attempt is found after the constraint violation
     */
    public Optional<PaymentAttemptEntity> findAfterDuplicateKey(UUID sourceEventId, UUID correlationId,
                                                               DataIntegrityViolationException exception) {
        Optional<PaymentAttemptEntity> existing = findExisting(sourceEventId, correlationId);
        if (existing.isPresent()) {
            return existing;
        }
        throw exception;
    }
}
