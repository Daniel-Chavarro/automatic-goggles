package org.java_avanzado.taller.utils.mapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReferenceMapper {

    @PersistenceContext
    private EntityManager entityManager;

    public ProductEntity longToProductEntity(Long id) {
        if (id == null) {
            return null;
        }
        return entityManager.getReference(ProductEntity.class, id);
    }

    public UserEntity uuidToUserEntity(UUID id) {
        if (id == null) {
            return null;
        }
        return entityManager.getReference(UserEntity.class, id);
    }
}