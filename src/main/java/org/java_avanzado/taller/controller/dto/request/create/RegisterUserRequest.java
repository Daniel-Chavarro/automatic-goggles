package org.java_avanzado.taller.controller.dto.request.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload used to register a new user account.
 */
@Data
public class RegisterUserRequest {

    /**
     * User's given name.
     */
    @NotNull
    @Max(value = 100, message = "First name cannot be more than 100 characters")
    private String firstName;

    /**
     * User's family name.
     */
    @NotNull
    @Max(value = 100, message = "Last name cannot be more than 100 characters")
    private String lastName;

    /**
     * User's email address used as a unique contact credential.
     */
    @NotNull
    @Email
    @Max(value = 320, message = "Email cannot be more than 100 characters")
    private String email;

    /**
     * Optional 10-digit phone number without separators.
     */
    @Pattern(regexp = "(^$|[0-9]{10})")
    private String phone;

    /**
     * Account password.
     * Must include at least one digit, one lowercase letter, and one uppercase letter.
     */
    @NotNull
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
            message = "Password must contain at least one digit, one lowercase, and one uppercase letter")
    private String password;

}
