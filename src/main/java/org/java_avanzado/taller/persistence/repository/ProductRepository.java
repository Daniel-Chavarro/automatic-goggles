package org.java_avanzado.taller.persistence.repository;

import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    List<ProductEntity> findAllByName(String name);
    Optional<ProductEntity> findByIdAndActiveTrue(Long id);
}
