package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.java_avanzado.taller.persistence.repository.EventLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    @Async
    public void logEvent(String eventType, String details, String username) {
        try {
            EventLogEntity log = EventLogEntity.builder()
                    .eventType(eventType)
                    .details(details)
                    .username(username)
                    .timestamp(LocalDateTime.now())
                    .build();
            eventLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to log event: {}", e.getMessage(), e);
        }
    }

    public List<EventLogEntity> getAllLogs() {
        return eventLogRepository.findAll();
    }
}