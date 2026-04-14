package org.java_avanzado.taller.persistence.repository;

import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface UserRepository extends JpaRepository<UserEntity, UUID> {
    List<UserEntity> findAllByFirstNameLikeIgnoreCase(String firstName);
    List<UserEntity> findAllByLastNameLikeIgnoreCase(String lastName);
    Optional<UserEntity> findByIdAndActive(UUID id, boolean active);
    Optional<UserEntity> findByEmail(String email);
}
