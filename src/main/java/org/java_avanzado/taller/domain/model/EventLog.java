package org.java_avanzado.taller.domain.model;


import org.java_avanzado.taller.domain.model.enums.EventType;

import java.time.LocalDateTime;

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
