package org.java_avanzado.taller.controller.dto.request.update;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 100, message = "First name cannot be more than 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name cannot be more than 100 characters")
    private String lastName;

    @Pattern(regexp = "(^$|[0-9]{10})", message = "Phone must be a 10-digit number")
    private String phone;
}
