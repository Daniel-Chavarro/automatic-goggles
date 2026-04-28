package org.java_avanzado.taller.persistence.repository;

import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity persistence operations.
 *
 * <p>Provides standard JPA CRUD operations and custom
 * queries for user search and retrieval.</p>
 */
public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
    /**
     * Searches for users by first name pattern (case-insensitive).
     */
    List<UserEntity> findAllByFirstNameLikeIgnoreCase(String firstName);

    /**
     * Searches for users by last name pattern (case-insensitive).
     */
    List<UserEntity> findAllByLastNameLikeIgnoreCase(String lastName);

    /**
     * Finds a user by ID with optional active status filter.
     */
    Optional<UserEntity> findByIdAndActive(UUID id, boolean active);

    /**
     * Finds a user by email address.
     */
    Optional<UserEntity> findByEmail(String email);
}
