package org.java_avanzado.taller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT tokens")
public record JwtAuthResponse(
        @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String accessToken,
        @Schema(description = "JWT refresh token for obtaining new access tokens", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String refreshToken,
        @Schema(description = "Token type", example = "Bearer") String tokenType
) {
    public JwtAuthResponse {
        if (tokenType == null) tokenType = "Bearer";
    }
}