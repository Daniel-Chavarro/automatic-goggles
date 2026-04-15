package org.java_avanzado.taller.controller;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.exception.UserNotFoundException;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        if (user.getEmail() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        String token = userService.registerUser(user);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        if (email == null || password == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        String token = userService.authenticate(email, password);
        return ResponseEntity.ok(Map.of("token", token));
    }
}