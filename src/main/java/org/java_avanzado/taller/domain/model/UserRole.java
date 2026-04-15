package org.java_avanzado.taller.domain.model;

import lombok.Getter;

/**
 * Defines authorization roles available for system users.
 */
@Getter
public enum UserRole {
    /**
     * Standard customer role with regular application permissions.
     */
    CLIENT("CLIENT"),

    /**
     * Administrative role with elevated permissions.
     */
    ADMIN("ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }
}