package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.auth.LoginUserRequest;
import org.java_avanzado.taller.controller.dto.request.auth.RegisterUserRequest;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.service.UserService;
import org.java_avanzado.taller.utils.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Exposes authentication endpoints for registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Registers a new user and returns the issued token.
     *
     * @param request user register data containing at least email and password
     * @return a response with the generated token
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody @Valid RegisterUserRequest request) {
        User user = userMapper.fromRegisterUserRequestToDomain(request);
        String token = userService.registerUser(user);
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * Authenticates a user with email and password and returns a token.
     *
     * @param credentials map containing the keys {@code email} and {@code password}
     * @return a response with the generated token
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid LoginUserRequest credentials) {
        String email = credentials.getEmail();
        String password = credentials.getPassword();

        String token = userService.authenticate(email, password);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
