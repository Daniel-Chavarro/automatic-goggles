package org.java_avanzado.taller.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.java_avanzado.taller.domain.model.enums.EventType;

import java.time.LocalDateTime;

/**
 * Entity that records security and audit events in the system.
 *
 * <p>Stores event metadata for security monitoring, including
 * login attempts, access violations, and other security-relevant
 * actions performed within the system.</p>
 */
@Entity
@Table(name = "event_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventLogEntity {
    /**
     * Technical identifier of the event log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type identifier for the event.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    /**
     * Additional details or context about the event.
     */
    @Column(nullable = false)
    private String details;

    /**
     * Moment when the event occurred.
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * Associated email, if applicable.
     */
    @Column
    private String email;
}
