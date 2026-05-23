package org.java_avanzado.taller.persistence.repository;

import org.java_avanzado.taller.persistence.entity.OrderProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for OrderProduct entity persistence operations.
 *
 * <p>Manages the join table between orders and products.</p>
 */
interface OrderProductRepository extends JpaRepository<OrderProductEntity, Long> {
}
