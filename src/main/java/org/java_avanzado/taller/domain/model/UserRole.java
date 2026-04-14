package org.java_avanzado.taller.domain.model;

/**
 * Defines authorization roles available for system users.
 */
public enum UserRole {
    /**
     * Standard customer role with regular application permissions.
     */
    CLIENT,

    /**
     * Administrative role with elevated permissions.
     */
    ADMIN,
}
