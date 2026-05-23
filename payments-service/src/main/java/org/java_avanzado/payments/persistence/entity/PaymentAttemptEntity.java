package org.java_avanzado.payments.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.java_avanzado.payments.domain.PaymentStatus;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttemptEntity {

    @Id
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "correlation_id", nullable = false, unique = true, updatable = false)
    private UUID correlationId;

    @Column(name = "source_event_id", nullable = false, unique = true, updatable = false)
    private UUID sourceEventId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "failure_code")
    private String failureCode;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PaymentAttemptEntity() {
    }

    public PaymentAttemptEntity(UUID paymentId,
                                Long orderId,
                                UUID correlationId,
                                UUID sourceEventId,
                                BigDecimal amount,
                                PaymentStatus status,
                                String failureCode,
                                String failureReason) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.correlationId = correlationId;
        this.sourceEventId = sourceEventId;
        this.amount = amount;
        this.status = status;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public UUID getSourceEventId() {
        return sourceEventId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
