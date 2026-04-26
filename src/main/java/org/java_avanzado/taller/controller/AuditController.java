package org.java_avanzado.taller.controller;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.java_avanzado.taller.service.EventLogService;
import org.java_avanzado.taller.controller.dto.request.filter.AuditFilterDto;
import org.java_avanzado.taller.controller.dto.response.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Returns a paginated and filtered list of audit log entries.
     *
     * @param filter   the filter criteria
     * @param pageable pagination and sorting information
     * @return the stored audit events
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<EventLogEntity>> getAuditLogs(
            AuditFilterDto filter, Pageable pageable) {
        Page<EventLogEntity> logs = eventLogService.getLogs(filter, pageable);
        return ResponseEntity.ok(PaginatedResponse.from(logs));
    }
}