package org.java_avanzado.taller.persistence.repository;

import java.util.UUID;
import org.java_avanzado.taller.persistence.entity.ProcessedPaymentResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedPaymentResultRepository extends JpaRepository<ProcessedPaymentResultEntity, UUID> {
}
