package org.java_avanzado.taller.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.java_avanzado.taller.support.AuditTestUtils.withAudit;

import org.java_avanzado.taller.domain.model.UserRole;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Nested
    class GivenUsers {

        @Nested
        class WhenFindingByEmail {

            @Test
            void given_existingUserEmail_when_findingByEmail_then_returnsMatchingUser() {
                UserEntity user = userRepository.save(buildUser("Ana", "Lopez", "ana@example.com", true));

                var result = userRepository.findByEmail("ana@example.com");

                assertThat(result).isPresent();
                assertThat(result.get().getId()).isEqualTo(user.getId());
                assertThat(result.get().getEmail()).isEqualTo("ana@example.com");
            }
        }

        @Nested
        class WhenFindingByFirstNameLikeIgnoreCase {

            @Test
            void given_usersWithDifferentCases_when_findingByFirstNameLikeIgnoreCase_then_returnsCaseInsensitiveMatches() {
                UserEntity expectedOne = userRepository.save(buildUser("Ana", "Lopez", "ana.1@example.com", true));
                UserEntity expectedTwo = userRepository.save(buildUser("ANABEL", "Ruiz", "ana.2@example.com", true));
                userRepository.save(buildUser("Carlos", "Lopez", "carlos@example.com", true));

                var results = userRepository.findAllByFirstNameLikeIgnoreCase("%an%");

                assertThat(results)
                        .extracting(UserEntity::getId)
                        .containsExactlyInAnyOrder(expectedOne.getId(), expectedTwo.getId());
            }
        }

        @Nested
        class WhenFindingByLastNameLikeIgnoreCase {

            @Test
            void given_usersWithDifferentCases_when_findingByLastNameLikeIgnoreCase_then_returnsCaseInsensitiveMatches() {
                UserEntity expectedOne = userRepository.save(buildUser("Ana", "Lopez", "ana.3@example.com", true));
                UserEntity expectedTwo = userRepository.save(buildUser("Luis", "LOPEZ RAMIREZ", "luis@example.com", true));
                userRepository.save(buildUser("Maria", "Perez", "maria@example.com", true));

                var results = userRepository.findAllByLastNameLikeIgnoreCase("%lopez%");

                assertThat(results)
                        .extracting(UserEntity::getId)
                        .containsExactlyInAnyOrder(expectedOne.getId(), expectedTwo.getId());
            }
        }

        @Nested
        class WhenFindingByIdAndActive {

            @Test
            void given_usersWithDifferentStatuses_when_findingByIdAndActive_then_matchesOnlyRequestedStatus() {
                UserEntity activeUser = userRepository.save(buildUser("Andrea", "Paz", "andrea@example.com", true));
                UserEntity inactiveUser = userRepository.save(buildUser("Bruno", "Paz", "bruno@example.com", false));

                var activeResult = userRepository.findByIdAndActive(activeUser.getId(), true);
                var inactiveWhenActiveRequested = userRepository.findByIdAndActive(inactiveUser.getId(), true);
                var inactiveResult = userRepository.findByIdAndActive(inactiveUser.getId(), false);

                assertThat(activeResult).isPresent();
                assertThat(activeResult.get().getId()).isEqualTo(activeUser.getId());
                assertThat(inactiveWhenActiveRequested).isEmpty();
                assertThat(inactiveResult).isPresent();
                assertThat(inactiveResult.get().getId()).isEqualTo(inactiveUser.getId());
            }
        }
    }

    private UserEntity buildUser(String firstName, String lastName, String email, boolean active) {
        return withAudit(UserEntity.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone("3001112233")
                .password("encoded-password")
                .role(UserRole.CLIENT)
                .active(active)
                .build());
    }
}
