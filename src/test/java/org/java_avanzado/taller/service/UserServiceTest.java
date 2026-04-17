package org.java_avanzado.taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.java_avanzado.taller.domain.exception.EmailAlreadyExistsException;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.utils.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Nested
    class GivenRegisterUser {

        @Test
        void given_validUser_when_registerUser_then_savesEncodedPasswordAndReturnsToken() {
            var user = TestDataFactory.user();
            UserEntity mappedEntity = TestDataFactory.userEntity();
            String email = user.getEmail();
            String rawPassword = user.getPassword();
            String encodedPassword = "encoded-password";
            String token = "jwt-token";

            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(userMapper.fromUserToEntity(user)).thenReturn(mappedEntity);
            when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
            when(jwtService.generateToken(email)).thenReturn(token);
            when(userRepository.save(mappedEntity)).thenReturn(mappedEntity);

            String result = userService.registerUser(user);

            assertEquals(token, result);

            ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(entityCaptor.capture());
            UserEntity persistedEntity = entityCaptor.getValue();
            assertEquals(encodedPassword, persistedEntity.getPassword());
            assertTrue(persistedEntity.isActive());
            verify(jwtService).generateToken(email);
        }

        @Test
        void given_existingEmail_when_registerUser_then_throwsEmailAlreadyExistsException() {
            var user = TestDataFactory.user();
            String email = user.getEmail();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(TestDataFactory.userEntity()));

            assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(user));

            verify(userRepository).findByEmail(email);
            verifyNoInteractions(userMapper, passwordEncoder, jwtService);
        }
    }

    @Nested
    class GivenAuthenticate {

        @Test
        void given_validCredentials_when_authenticate_then_returnsToken() {
            UserEntity userEntity = TestDataFactory.userEntity();
            var user = TestDataFactory.user();
            String email = user.getEmail();
            String rawPassword = user.getPassword();
            String storedPassword = userEntity.getPassword();
            String token = "jwt-token";

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
            when(passwordEncoder.matches(rawPassword, storedPassword)).thenReturn(true);
            when(jwtService.generateToken(email)).thenReturn(token);

            String result = userService.authenticate(email, rawPassword);

            assertEquals(token, result);
            verify(userRepository).findByEmail(email);
            verify(passwordEncoder).matches(rawPassword, storedPassword);
            verify(jwtService).generateToken(email);
        }

        @Test
        void given_invalidPassword_when_authenticate_then_throwsIllegalArgumentException() {
            UserEntity userEntity = TestDataFactory.userEntity();
            var user = TestDataFactory.user();
            String email = user.getEmail();
            String rawPassword = user.getPassword();
            String storedPassword = userEntity.getPassword();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
            when(passwordEncoder.matches(rawPassword, storedPassword)).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () -> userService.authenticate(email, rawPassword));

            verify(userRepository).findByEmail(email);
            verify(passwordEncoder).matches(rawPassword, storedPassword);
            verifyNoInteractions(jwtService);
        }

        @Test
        void given_inactiveUser_when_authenticate_then_throwsIllegalArgumentException() {
            UserEntity inactiveUserEntity = TestDataFactory.userEntity();
            inactiveUserEntity.setActive(false);
            var user = TestDataFactory.user();
            String email = user.getEmail();
            String rawPassword = user.getPassword();
            String storedPassword = inactiveUserEntity.getPassword();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(inactiveUserEntity));
            when(passwordEncoder.matches(rawPassword, storedPassword)).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> userService.authenticate(email, rawPassword));

            verify(userRepository).findByEmail(email);
            verify(passwordEncoder).matches(rawPassword, storedPassword);
            verifyNoInteractions(jwtService);
            verifyNoMoreInteractions(userRepository, passwordEncoder);
        }
    }
}
