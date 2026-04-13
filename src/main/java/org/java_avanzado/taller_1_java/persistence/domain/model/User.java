package org.java_avanzado.taller_1_java.persistence.domain.model;

import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller_1_java.domain.model.UserRole;

import java.util.UUID;

@Data
@Builder
public class User {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private UserRole role;
    private boolean active;
}
