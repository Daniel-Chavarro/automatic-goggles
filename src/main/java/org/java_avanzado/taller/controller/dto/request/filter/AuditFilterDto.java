package org.java_avanzado.taller.controller.dto.request.filter;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditFilterDto {
    private String eventType;
    private String username;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
