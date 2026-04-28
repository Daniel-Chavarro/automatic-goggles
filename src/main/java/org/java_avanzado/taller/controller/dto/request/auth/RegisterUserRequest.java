package org.java_avanzado.taller.controller.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload used to register a new user account.
 */
@Data
@Schema(description = "Payload for registering a new user account")
public class RegisterUserRequest {

    /**
     * User's given name.
     */
    @Schema(description = "User's first name", example = "John", maxLength = 100)
    @NotNull
    @Size(max = 100, message = "First name cannot be more than 100 characters")
    private String firstName;

    /**
     * User's family name.
     */
    @Schema(description = "User's last name", example = "Doe", maxLength = 100)
    @NotNull
    @Size(max = 100, message = "Last name cannot be more than 100 characters")
    private String lastName;

    /**
     * User's email address used as a unique contact credential.
     */
    @Schema(description = "User's email address", example = "john.doe@example.com", maxLength = 320)
    @NotNull
    @Email
    @Size(max = 320, message = "Email cannot be more than 100 characters")
    private String email;

    /**
     * Optional 10-digit phone number without separators.
     */
    @Schema(description = "Optional 10-digit phone number", example = "1234567890")
    @Pattern(regexp = "(^$|[0-9]{10})")
    private String phone;

    /**
     * Account password.
     * Must include at least one digit, one lowercase letter, and one uppercase letter.
     */
    @Schema(description = "Account password (must contain digit, lowercase, and uppercase)",
            example = "Password123",
            minLength = 8,
            maxLength = 255,
            pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$")
    @NotNull
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
            message = "Password must contain at least one digit, one lowercase, and one uppercase letter")
    private String password;
}