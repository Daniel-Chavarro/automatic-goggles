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
import org.java_avanzado.taller.controller.dto.request.create.CreateUserRequest;
import org.java_avanzado.taller.controller.dto.request.filter.UserFilterDto;
import org.java_avanzado.taller.controller.dto.request.update.UpdateUserRequest;
import org.java_avanzado.taller.controller.dto.response.PaginatedResponse;
import org.java_avanzado.taller.controller.dto.response.UserResponse;
import org.java_avanzado.taller.controller.dto.response.UserSummaryResponse;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.service.UserService;
import org.java_avanzado.taller.utils.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Users", description = "User management operations")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "List all users",
            description = "Returns a paginated list of users with optional filtering by first name, " +
                    "last name, email, role, and active status." +
                    "\nRequires ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PaginatedResponse<UserSummaryResponse>> getAllUsers(
            @Parameter(description = "Filter criteria for users") UserFilterDto filter,
            @Parameter(description = "Pagination and sorting information") Pageable pageable) {
        Page<User> users = userService.getUsers(filter, pageable);
        Page<UserSummaryResponse> responsePage = users.map(userMapper::fromUserToSummary);
        return ResponseEntity.ok(PaginatedResponse.from(responsePage));
    }

    @Operation(summary = "Get user by ID",
            description = "Returns a single user by their unique identifier." +
                    "\nRequires ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "Unique identifier of the user", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(userMapper.fromUserToResponse(userService.getUserById(id)));
    }

    @Operation(summary = "Create a new user",
            description = "Creates a new user with the provided information. The email must be unique." +
                    "\nRequires ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User creation payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateUserRequest.class)))
            @Valid @RequestBody CreateUserRequest request) {
        User user = userMapper.fromCreateUserRequestToDomain(request);
        return ResponseEntity.ok(userMapper.fromUserToResponse(userService.createUser(user)));
    }

    @Operation(summary = "Update user",
            description = "Partially updates an existing user. Only provided fields will be updated." +
                    "\nRequires ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "Unique identifier of the user", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Fields to update",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateUserRequest.class)))
            @Valid @RequestBody UpdateUserRequest request) {
        User user = userMapper.fromUpdateUserRequestToDomain(request);
        return ResponseEntity.ok(userMapper.fromUserToResponse(userService.updateUser(id, user)));
    }

    @Operation(summary = "Disable a user",
            description = "Disables a user account. The user will no longer be able to access the system." +
                    "\nRequires ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User disabled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableUser(
            @Parameter(description = "Unique identifier of the user", required = true) @PathVariable UUID id) {
        userService.disableUser(id);
        return ResponseEntity.noContent().build();
    }
}