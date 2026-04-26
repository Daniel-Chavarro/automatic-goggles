package org.java_avanzado.taller.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.auth.LoginUserRequest;
import org.java_avanzado.taller.controller.dto.request.auth.RegisterUserRequest;
import org.java_avanzado.taller.controller.dto.response.JwtAuthResponse;
import org.java_avanzado.taller.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Authentication and authorization operations")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Creates a new user account with email, password, and profile information. Requires email uniqueness.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterUserRequest.class)))
            @RequestBody @Valid RegisterUserRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Authenticate user", description = "Logs in a user with email and password, returning JWT access and refresh tokens.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginUserRequest.class)))
            @RequestBody @Valid LoginUserRequest credentials) {
        return ResponseEntity.ok(authService.login(credentials));
    }

    @Operation(summary = "Refresh access token", description = "Obtains a new access token using a valid refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refresh(
            @Parameter(description = "Valid refresh token", required = true) @RequestBody String refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @Operation(summary = "Logout user", description = "Invalidates the refresh token, effectively logging out the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Refresh token to invalidate", required = true) @RequestBody String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }
}