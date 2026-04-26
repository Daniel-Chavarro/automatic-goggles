package org.java_avanzado.taller.persistence.specification;

import jakarta.persistence.criteria.Predicate;
import org.java_avanzado.taller.controller.dto.request.filter.UserFilterDto;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    public static Specification<UserEntity> withFilter(UserFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getFirstName() != null && !filter.getFirstName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + filter.getFirstName().toLowerCase() + "%"));
            }

            if (filter.getLastName() != null && !filter.getLastName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + filter.getLastName().toLowerCase() + "%"));
            }

            if (filter.getEmail() != null && !filter.getEmail().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + filter.getEmail().toLowerCase() + "%"));
            }

            if (filter.getRole() != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), filter.getRole()));
            }

            if (filter.getActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), filter.getActive()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
