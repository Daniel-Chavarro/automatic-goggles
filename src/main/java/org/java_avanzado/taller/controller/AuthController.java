package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.auth.LoginUserRequest;
import org.java_avanzado.taller.controller.dto.request.auth.RegisterUserRequest;
import org.java_avanzado.taller.controller.dto.response.JwtAuthResponse;
import org.java_avanzado.taller.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(@RequestBody @Valid RegisterUserRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody @Valid LoginUserRequest credentials) {
        return ResponseEntity.ok(authService.login(credentials));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refresh(@RequestBody String refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }
}