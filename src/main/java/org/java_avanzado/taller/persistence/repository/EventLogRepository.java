package org.java_avanzado.taller.persistence.repository;

import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<EventLogEntity, Long> {
}
