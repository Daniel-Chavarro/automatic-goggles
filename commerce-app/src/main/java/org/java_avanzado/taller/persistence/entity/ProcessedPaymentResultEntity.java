package org.java_avanzado.taller.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "processed_payment_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedPaymentResultEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "result_type", nullable = false, updatable = false, length = 32)
    private String resultType;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    @PrePersist
    void prePersist() {
        if (processedAt == null) {
            processedAt = Instant.now();
        }
    }
}
