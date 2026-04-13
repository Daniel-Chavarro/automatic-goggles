package org.java_avanzado.taller_1_java.persistence.repository;

import org.java_avanzado.taller_1_java.persistence.entity.OrderProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderProductRepository extends JpaRepository<OrderProductEntity, Long> {
}
