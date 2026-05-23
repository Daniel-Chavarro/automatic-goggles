package org.java_avanzado.taller.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.filter.AuditFilterDto;
import org.java_avanzado.taller.controller.dto.response.PaginatedResponse;
import org.java_avanzado.taller.domain.model.EventLog;
import org.java_avanzado.taller.service.EventLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for audit log retrieval operations.
 *
 * <p>Provides endpoints for retrieving and filtering audit log entries
 * that track all system events and changes. Requires ADMIN privileges.</p>
 */
@Tag(name = "Audit", description = "Audit log retrieval operations")
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final EventLogService eventLogService;

    @Operation(summary = "Get audit logs",
            description = "Returns a paginated list of audit log entries with optional " +
                    "filtering by event type, username, and date range." +
                    "\nRequires ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PaginatedResponse<EventLog>> getAuditLogs(
            @Parameter(description = "Filter criteria for audit logs") AuditFilterDto filter,
            @Parameter(description = "Pagination and sorting information") Pageable pageable) {
        Page<EventLog> logs = eventLogService.getLogs(filter, pageable);
        return ResponseEntity.ok(PaginatedResponse.from(logs));
    }
}