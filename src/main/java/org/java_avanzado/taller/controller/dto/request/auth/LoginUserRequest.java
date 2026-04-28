package org.java_avanzado.taller.controller.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload used to authenticate a user.
 */
@Data
@Schema(description = "Payload for authenticating a user")
public class LoginUserRequest {

    /**
     * User email address used as the login identifier.
     */
    @Schema(description = "User email address", example = "john.doe@example.com")
    @NotNull
    @Email
    private String email;

    /**
     * User password used to validate credentials.
     */
    @Schema(description = "User password", example = "Password123")
    @NotNull
    private String password;
}