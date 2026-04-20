package org.java_avanzado.taller.controller;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.java_avanzado.taller.service.EventLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes audit log retrieval endpoints.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final EventLogService eventLogService;

    /**
     * Returns all audit log entries.
     *
     * @return the stored audit events
     */
    @GetMapping
    public ResponseEntity<List<EventLogEntity>> getAuditLogs() {
        return ResponseEntity.ok(eventLogService.getAllLogs());
    }
}