package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_avanzado.taller.controller.dto.request.filter.AuditFilterDto;
import org.java_avanzado.taller.domain.model.EventLog;
import org.java_avanzado.taller.domain.model.enums.EventType;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.java_avanzado.taller.persistence.repository.EventLogRepository;
import org.java_avanzado.taller.persistence.specification.AuditSpecifications;
import org.java_avanzado.taller.utils.mapper.EventLogMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for logging and retrieving audit events.
 *
 * <p>Provides asynchronous event logging for security monitoring
 * and audit trail purposes.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventLogService {

    private final EventLogRepository eventLogRepository;
    private final EventLogMapper eventLogMapper;

    @Async
    public void logEvent(EventType eventType, String email) {
        try {
            EventLogEntity log = EventLogEntity.builder()
                    .eventType(eventType)
                    .details(eventType.getDescription())
                    .email(email)
                    .timestamp(LocalDateTime.now())
                    .build();
            eventLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to log event: {}", e.getMessage(), e);
        }
    }

    public List<EventLog> getAllLogs() {
        return eventLogRepository.findAll().stream()
                .map(eventLogMapper::fromEventLogEntityToDomain)
                .collect(Collectors.toList());
    }

    public Page<EventLog> getLogs(AuditFilterDto filter, Pageable pageable) {
        Specification<EventLogEntity> spec = AuditSpecifications.withFilter(filter);
        Page<EventLogEntity> logs = eventLogRepository.findAll(spec, pageable);
        return logs.map(eventLogMapper::fromEventLogEntityToDomain);
    }
}