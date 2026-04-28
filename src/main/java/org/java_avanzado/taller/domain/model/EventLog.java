package org.java_avanzado.taller.domain.model;


import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.model.enums.EventType;

import java.time.LocalDateTime;

@Data
@Builder
public class EventLog {
    /**
     * Technical identifier of the event log entry.
     */
    private Long id;

    /**
     * Type identifier for the event.
     */
    private EventType eventType;

    /**
     * Additional details or context about the event.
     */
    private String details;

    /**
     * Moment when the event occurred.
     */
    private LocalDateTime timestamp;

    /**
     * Associated email, if applicable.
     */
    private String email;
}
