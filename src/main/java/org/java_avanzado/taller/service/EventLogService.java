package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.java_avanzado.taller.persistence.repository.EventLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventLogService {

    private final EventLogRepository eventLogRepository;

    @Async
    public void logEvent(String eventType, String details, String username) {
        EventLogEntity log = EventLogEntity.builder()
                .eventType(eventType)
                .details(details)
                .username(username)
                .timestamp(LocalDateTime.now())
                .build();
        eventLogRepository.save(log);
    }

    public List<EventLogEntity> getAllLogs() {
        return eventLogRepository.findAll();
    }
}