package org.java_avanzado.taller.domain.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Defines authorization roles available for system users.
 */
@Getter
@Schema(description = "User roles in the system",
        allowableValues = {"CLIENT", "ADMIN"})
public enum UserRole {

    /**
     * Standard customer role with regular application permissions.
     */
    @Schema(description = "Standard customer role with regular application permissions")
    CLIENT,

    /**
     * Administrative role with elevated permissions.
     */
    @Schema(description = "Administrative role with elevated permissions")
    ADMIN
}