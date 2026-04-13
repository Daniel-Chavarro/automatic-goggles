package org.java_avanzado.taller_1_java.controller.dto.request.create;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterUserRequest {

    @NotNull
    @Max(value = 100, message = "First name cannot be more than 100 characters")
    private String firstName;
    @NotNull
    @Max(value = 100, message = "Last name cannot be more than 100 characters")
    private String lastName;
    @NotNull
    @Email
    @Max(value = 320, message = "Email cannot be more than 100 characters")
    private String email;
    @Pattern(regexp="(^$|[0-9]{10})")
    private String phone;
    @NotNull
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
            message = "Password must contain at least one digit, one lowercase, and one uppercase letter")
    private String password;

}
