package org.java_avanzado.taller.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.model.enums.UserRole;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed user information response")
public class UserResponse {
     
    @Schema(description = "Unique identifier of the user", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    
    @Schema(description = "User's first name", example = "John")
    private String firstName;
    
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "User's phone number (10 digits)", example = "1234567890")
    private String phone;
    
    @Schema(description = "User's role in the system")
    private UserRole role;
    
    @Schema(description = "Whether the user account is active", example = "true")
    private boolean active;
    
    @Schema(description = "Timestamp when the user was created", example = "2026-04-26T10:30:00Z")
    private Instant createdAt;
}