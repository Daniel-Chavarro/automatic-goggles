package org.java_avanzado.taller.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines authorization roles available for system users.
 */
@Getter
@RequiredArgsConstructor
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
}
