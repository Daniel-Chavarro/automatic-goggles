package org.java_avanzado.taller.domain.model;

import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.model.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing an application user account.
 */
@Data
@Builder
public class User {
    /**
     * Unique identifier for the user account.
     */
    private UUID id;

    /**
     * User's given name.
     */
    private String firstName;

    /**
     * User's family name.
     */
    private String lastName;

    /**
     * Unique email used to identify and contact the user.
     */
    private String email;

    /**
     * Optional contact phone number.
     */
    private String phone;

    /**
     * Credential hash or password value depending on storage strategy.
     */
    private String password;

    /**
     * Authorization role assigned to the user.
     */
    @Builder.Default
    private UserRole role = UserRole.CLIENT;

    /**
     * Indicates whether the user is currently active.
     */
    @Builder.Default
    private boolean active = true;

    /**
     * Timestamp when the user was created.
     */
    private Instant createdAt;

    /**
     * Timestamp of the last update to the user.
     */
    private Instant updatedAt;
}
