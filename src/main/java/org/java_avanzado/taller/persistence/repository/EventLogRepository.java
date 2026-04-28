package org.java_avanzado.taller.persistence.repository;

import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository for EventLog entity persistence operations.
 *
 * <p>Provides standard JPA CRUD operations for audit event logging.</p>
 */
public interface EventLogRepository extends JpaRepository<EventLogEntity, Long>, JpaSpecificationExecutor<EventLogEntity> {
}
