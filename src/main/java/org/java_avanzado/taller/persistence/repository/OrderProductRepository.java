package org.java_avanzado.taller.persistence.repository;

import org.java_avanzado.taller.persistence.entity.OrderProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderProductRepository extends JpaRepository<OrderProductEntity, Long> {
}
