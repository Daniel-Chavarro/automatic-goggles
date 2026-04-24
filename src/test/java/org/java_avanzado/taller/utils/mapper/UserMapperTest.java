package org.java_avanzado.taller.utils.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.domain.model.enums.UserRole;
import org.java_avanzado.taller.support.TestDataFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Nested
    class GivenUserEntity {

        @Test
        void given_userEntity_when_mappingToDomain_then_coreFieldsAreMapped() {
            var entity = TestDataFactory.userEntity();

            var result = mapper.fromUserEntityToDomain(entity);

            assertEquals(entity.getId(), result.getId());
            assertEquals(entity.getFirstName(), result.getFirstName());
            assertEquals(entity.getLastName(), result.getLastName());
            assertEquals(entity.getEmail(), result.getEmail());
            assertEquals(entity.getPhone(), result.getPhone());
            assertEquals(entity.getRole(), result.getRole());
            assertEquals(entity.isActive(), result.isActive());
        }
    }

    @Nested
    class GivenUpdateRequestAndExistingUser {

        @Test
        void given_updateRequestAndExistingUser_when_mappingToDomain_then_emailRoleAndPasswordArePreservedWhileMutableFieldsAreUpdated() {
            var request = TestDataFactory.updateUserRequest();
            User existing = User.builder()
                    .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                    .firstName("Old")
                    .lastName("Name")
                    .email("kept@example.com")
                    .phone("3000000000")
                    .password("kept-hash")
                    .role(UserRole.ADMIN)
                    .active(true)
                    .build();

            var result = mapper.fromUpdateUserRequestToDomain(request, existing);

            assertEquals(existing.getEmail(), result.getEmail());
            assertEquals(existing.getRole(), result.getRole());
            assertEquals(existing.getPassword(), result.getPassword());
            assertEquals(request.getFirstName(), result.getFirstName());
            assertEquals(request.getLastName(), result.getLastName());
            assertEquals(request.getPhone(), result.getPhone());
        }
    }
}
