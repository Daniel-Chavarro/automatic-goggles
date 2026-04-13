package org.java_avanzado.taller_1_java.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base class for automatic auditing of JPA entities.
 *
 * <p>Entities that extend this class inherit traceability columns for creation
 * and updates. Values are managed by Spring Data JPA when auditing is enabled
 * in the application.</p>
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AuditableEntity {

    /** Timestamp when the record was created. */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of the last record update. */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** Identifier of the actor who created the record. */
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    /** Identifier of the actor who made the last update. */
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
}
