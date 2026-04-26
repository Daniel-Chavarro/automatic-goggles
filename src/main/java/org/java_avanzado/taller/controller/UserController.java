package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.CreateUserRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateUserRequest;
import org.java_avanzado.taller.controller.dto.response.UserResponse;
import org.java_avanzado.taller.controller.dto.response.UserSummaryResponse;
import org.java_avanzado.taller.controller.dto.request.filter.UserFilterDto;
import org.java_avanzado.taller.controller.dto.response.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.service.UserService;
import org.java_avanzado.taller.utils.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Exposes user management endpoints.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Returns all users with filtering and pagination.
     *
     * @param filter   filter criteria
     * @param pageable pagination and sorting information
     * @return a paginated list of user summaries
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<UserSummaryResponse>> getAllUsers(
            UserFilterDto filter, Pageable pageable) {
        Page<User> users = userService.getUsers(filter, pageable);
        Page<UserSummaryResponse> responsePage = users.map(userMapper::fromUserToSummary);
        return ResponseEntity.ok(PaginatedResponse.from(responsePage));
    }

    /**
     * Returns a user by identifier.
     *
     * @param id user identifier
     * @return the user details
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userMapper.fromUserToResponse(userService.getUserById(id)));
    }

    /**
     * Creates a user.
     *
     * @param request validated user creation payload
     * @return the created user
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userMapper.fromCreateUserRequestToDomain(request);
        return ResponseEntity.ok(userMapper.fromUserToResponse(userService.createUser(user)));
    }

    /**
     * Updates partially an existing user.
     *
     * @param id      user identifier
     * @param request validated update payload
     * @return the updated user
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = userMapper.fromUpdateUserRequestToDomain(request);
        return ResponseEntity.ok(userMapper.fromUserToResponse(userService.updateUser(id, user)));
    }

    /**
     * Disables a user.
     *
     * @param id user identifier
     * @return an empty response with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableUser(@PathVariable UUID id) {
        userService.disableUser(id);
        return ResponseEntity.noContent().build();
    }
}