package org.java_avanzado.taller.controller.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload used to authenticate a user.
 */
@Data
public class LoginUserRequest {

    /**
     * User email address used as the login identifier.
     */
    @NotNull
    @Email
    private String email;

    /**
     * User password used to validate credentials.
     */
    @NotNull
    private String password;
}
