package org.java_avanzado.taller.controller.dto.request.filter;

import lombok.Data;
import org.java_avanzado.taller.domain.model.enums.UserRole;

@Data
public class UserFilterDto {
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private Boolean active;
}
