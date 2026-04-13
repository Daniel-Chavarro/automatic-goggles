package org.java_avanzado.taller_1_java.persistence.repository;

import org.java_avanzado.taller_1_java.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface UserRepository extends JpaRepository<UserEntity, UUID> {
}
