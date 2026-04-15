package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toDomain(UserEntity entity);
    UserEntity toEntity(User domain);
}