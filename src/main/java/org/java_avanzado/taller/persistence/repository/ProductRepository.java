package org.java_avanzado.taller.persistence.repository;

import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity persistence operations.
 *
 * <p>Provides standard JPA CRUD operations and custom
 * queries for product search and retrieval.</p>
 */
public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {
    /**
     * Finds all products matching the given name.
     */
    Optional<ProductEntity> findByName(String name);

    /**
     * Finds an active product by ID.
     */
    Optional<ProductEntity> findByIdAndActiveTrue(Long id);
}
