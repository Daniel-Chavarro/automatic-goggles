package org.java_avanzado.taller.support;

import java.time.Instant;
import org.java_avanzado.taller.persistence.entity.AuditableEntity;
import org.springframework.test.util.ReflectionTestUtils;

public final class AuditTestUtils {

    private AuditTestUtils() {}

    public static <T extends AuditableEntity> T withAudit(T entity) {
        Instant now = Instant.now();
        ReflectionTestUtils.setField(entity, "createdAt", now);
        ReflectionTestUtils.setField(entity, "createdBy", "repository-test");
        ReflectionTestUtils.setField(entity, "updatedAt", now);
        ReflectionTestUtils.setField(entity, "updatedBy", "repository-test");
        return entity;
    }
}
