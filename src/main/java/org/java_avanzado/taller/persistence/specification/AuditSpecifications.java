package org.java_avanzado.taller.persistence.specification;

import jakarta.persistence.criteria.Predicate;
import org.java_avanzado.taller.controller.dto.request.filter.AuditFilterDto;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuditSpecifications {

    public static Specification<EventLogEntity> withFilter(AuditFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getEventType() != null && !filter.getEventType().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("eventType"), filter.getEventType()));
            }

            if (filter.getUsername() != null && !filter.getUsername().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")),
                        "%" + filter.getUsername().toLowerCase() + "%"));
            }

            if (filter.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), filter.getToDate()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
