package org.java_avanzado.taller.persistence.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :id")
    Optional<OrderEntity> findByIdForPaymentResultUpdate(@Param("id") Long id);
}
