package org.java_avanzado.taller.persistence.repository;

import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Order entity persistence operations.
 *
 * <p>Provides standard JPA CRUD operations and custom
 * queries for order retrieval.</p>
 */
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {
    /**
     * Finds all orders for a specific user.
     */
    List<OrderEntity> findAllByUserId(UUID user_id);
}
