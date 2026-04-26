package org.java_avanzado.taller.controller.dto.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.java_avanzado.taller.controller.dto.request.auth.RegisterUserRequest;
import org.java_avanzado.taller.domain.model.enums.UserRole;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "Payload for creating a new user")
public class CreateUserRequest extends RegisterUserRequest {

    @Schema(description = "User's role in the system", requiredMode = Schema.RequiredMode.REQUIRED, example = "CLIENT")
    @NotNull
    private UserRole role;
}