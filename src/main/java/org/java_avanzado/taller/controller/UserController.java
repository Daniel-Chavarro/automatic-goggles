package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.CreateUserRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateUserRequest;
import org.java_avanzado.taller.controller.dto.response.UserResponse;
import org.java_avanzado.taller.controller.dto.response.UserSummaryResponse;
import org.java_avanzado.taller.service.UserService;
import org.java_avanzado.taller.utils.mapper.UserResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserResponseMapper userResponseMapper;

    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        return ResponseEntity.ok(userResponseMapper.toSummaryList(userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userResponseMapper.toResponse(userService.getUserById(id)));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userResponseMapper.toResponse(userService.createUser(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userResponseMapper.toResponse(userService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableUser(@PathVariable UUID id) {
        userService.disableUser(id);
        return ResponseEntity.noContent().build();
    }
}