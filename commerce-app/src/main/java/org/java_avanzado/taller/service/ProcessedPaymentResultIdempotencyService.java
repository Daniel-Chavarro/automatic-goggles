package org.java_avanzado.taller.service;

import java.util.UUID;
import org.java_avanzado.taller.persistence.entity.ProcessedPaymentResultEntity;
import org.java_avanzado.taller.persistence.repository.ProcessedPaymentResultRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing idempotency of processed payment results.
 *
 * <p>Ensures that payment result events are processed only once, even if received
 * multiple times. Tracks processed payment results to prevent duplicate order updates.</p>
 */
@Service
public class ProcessedPaymentResultIdempotencyService {

    private final ProcessedPaymentResultRepository processedPaymentResultRepository;

    public ProcessedPaymentResultIdempotencyService(ProcessedPaymentResultRepository processedPaymentResultRepository) {
        this.processedPaymentResultRepository = processedPaymentResultRepository;
    }

    /**
     * Marks an unknown order result as processed if it hasn't been already.
     *
     * <p>Records that a payment result event has been processed for an order that
     * couldn't be found. Uses a new transaction and returns true if the record was
     * created (indicating this is the first time), false otherwise (duplicate).</p>
     *
     * @param eventId the unique ID of the payment result event
     * @param orderId the ID of the order
     * @param resultType the type of result (SUCCEEDED or FAILED)
     * @return true if the result was marked as new, false if it was already marked
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markUnknownOrderProcessedIfNew(UUID eventId, Long orderId, String resultType) {
        if (processedPaymentResultRepository.existsById(eventId)) {
            return false;
        }

        try {
            processedPaymentResultRepository.saveAndFlush(ProcessedPaymentResultEntity.builder()
                    .eventId(eventId)
                    .orderId(orderId)
                    .resultType(resultType)
                    .build());
            return true;
        } catch (DataIntegrityViolationException exception) {
            return false;
        }
    }
}
