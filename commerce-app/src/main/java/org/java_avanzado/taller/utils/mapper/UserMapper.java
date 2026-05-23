package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.controller.dto.request.auth.RegisterUserRequest;
import org.java_avanzado.taller.controller.dto.request.create.CreateUserRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateUserRequest;
import org.java_avanzado.taller.controller.dto.response.UserResponse;
import org.java_avanzado.taller.controller.dto.response.UserSummaryResponse;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for converting User entities, domain models, and DTOs.
 *
 * <p>Provides methods for bidirectional mapping between UserEntity persistence objects,
 * User domain models, and UserResponse/UserSummaryResponse DTOs. Supports mapping from
 * various request types including registration, creation, and update requests.</p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    // Entity <-> Domain
    User fromUserEntityToDomain(UserEntity entity);

    UserEntity fromUserToEntity(User domain);

    void updateEntityFromDomain(User domain, @MappingTarget UserEntity entity);

    // Create Request -> Domain
    User fromCreateUserRequestToDomain(CreateUserRequest request);

    User fromRegisterUserRequestToDomain(RegisterUserRequest request);

    // Update Request -> Domain model
    User fromUpdateUserRequestToDomain(UpdateUserRequest request);

    // Domain -> Response
    UserResponse fromUserToResponse(User user);

    UserSummaryResponse fromUserToSummary(User user);

    // List variants
    List<UserResponse> fromUserListToResponseList(List<User> users);

    List<UserSummaryResponse> fromUserListToSummaryList(List<User> users);
}
