package org.java_avanzado.taller.persistence.specification;

import jakarta.persistence.criteria.Predicate;
import org.java_avanzado.taller.controller.dto.request.filter.OrderFilterDto;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecifications {

    public static Specification<OrderEntity> withFilter(OrderFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getMinTotal() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalPrice"), filter.getMinTotal()));
            }

            if (filter.getMaxTotal() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalPrice"), filter.getMaxTotal()));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("orderStatus"), filter.getStatus()));
            }

            if (filter.getActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), filter.getActive()));
            }

            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filter.getUserId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
