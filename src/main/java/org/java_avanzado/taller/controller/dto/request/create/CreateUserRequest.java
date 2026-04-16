package org.java_avanzado.taller.controller.dto.request.create;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.java_avanzado.taller.domain.model.UserRole;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateUserRequest extends RegisterUserRequest{
    @NotNull
    private UserRole role;
}
