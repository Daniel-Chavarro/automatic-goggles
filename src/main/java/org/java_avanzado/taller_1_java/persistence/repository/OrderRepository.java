package org.java_avanzado.taller_1_java.persistence.repository;

import org.java_avanzado.taller_1_java.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findAllByUserId(UUID user_id);
}
