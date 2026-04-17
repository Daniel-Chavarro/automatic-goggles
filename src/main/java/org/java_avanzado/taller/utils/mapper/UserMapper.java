package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.controller.dto.request.create.CreateUserRequest;
import org.java_avanzado.taller.controller.dto.request.create.RegisterUserRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateUserRequest;
import org.java_avanzado.taller.controller.dto.response.UserResponse;
import org.java_avanzado.taller.controller.dto.response.UserSummaryResponse;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    // Entity <-> Domain
    User fromUserEntityToDomain(UserEntity entity);
    UserEntity fromUserToEntity(User domain);

    // Create Request -> Domain
    User fromCreateUserRequestToDomain(CreateUserRequest request);
    User fromRegisterUserRequestToDomain(RegisterUserRequest request);

    // Update Request -> Domain (merge with existing)
    @Mapping(source = "existingUser.id", target = "id")
    @Mapping(source = "existingUser.password", target = "password")
    @Mapping(source = "existingUser.role", target = "role")
    @Mapping(source = "existingUser.email", target = "email")
    @Mapping(source = "existingUser.active", target = "active")
    @Mapping(source = "request.firstName", target = "firstName")
    @Mapping(source = "request.lastName", target = "lastName")
    @Mapping(source = "request.phone", target = "phone")
    User fromUpdateUserRequestToDomain(UpdateUserRequest request, User existingUser);

    // Domain -> Response
    UserResponse fromUserToResponse(User user);
    UserSummaryResponse fromUserToSummary(User user);

    // List variants
    List<UserResponse> fromUserListToResponseList(List<User> users);
    List<UserSummaryResponse> fromUserListToSummaryList(List<User> users);
}
