package org.java_avanzado.payments.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import org.java_avanzado.payments.persistence.entity.PaymentAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttemptEntity, UUID> {
    Optional<PaymentAttemptEntity> findBySourceEventIdOrCorrelationId(UUID sourceEventId, UUID correlationId);
}
