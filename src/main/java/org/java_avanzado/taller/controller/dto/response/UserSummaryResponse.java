package org.java_avanzado.taller.controller.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserSummaryResponse {
    
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}