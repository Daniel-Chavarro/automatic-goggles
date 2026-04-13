package org.java_avanzado.taller_1_java.persistence.repository;

import org.java_avanzado.taller_1_java.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
