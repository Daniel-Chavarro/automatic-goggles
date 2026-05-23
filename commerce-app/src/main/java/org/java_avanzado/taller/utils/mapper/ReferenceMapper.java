package org.java_avanzado.taller.utils.mapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Component for converting ID values to JPA entity references.
 *
 * <p>This mapper uses the EntityManager to create lazy-loading entity references
 * without fetching the full entity from the database. It handles null values gracefully.</p>
 */
@Component
public class ReferenceMapper {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Converts a Product ID to a ProductEntity reference.
     *
     * @param id the product ID
     * @return a lazy-loaded ProductEntity reference, or null if id is null
     */
    public ProductEntity longToProductEntity(Long id) {
        if (id == null) {
            return null;
        }
        return entityManager.getReference(ProductEntity.class, id);
    }

    /**
     * Converts a User ID to a UserEntity reference.
     *
     * @param id the user ID
     * @return a lazy-loaded UserEntity reference, or null if id is null
     */
    public UserEntity uuidToUserEntity(UUID id) {
        if (id == null) {
            return null;
        }
        return entityManager.getReference(UserEntity.class, id);
    }
}