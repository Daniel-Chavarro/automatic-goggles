package org.java_avanzado.taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.objenesis.ObjenesisStd;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new ObjenesisStd().newInstance(UserService.class);
        ReflectionTestUtils.setField(userService, "userRepository", userRepository);
        ReflectionTestUtils.setField(userService, "userMapper", userMapper);
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(userService, "jwtService", jwtService);
    }

    @Nested
    class GivenRegisterUser {

        @Test
        void given_validUser_when_registerUser_then_savesEncodedPasswordAndReturnsToken() {
            var user = TestDataFactory.user();
            UserEntity mappedEntity = TestDataFactory.userEntity();
            String email = "ana@example.com";
            String rawPassword = "StrongPass1";
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
            assertEquals(encodedPassword, ReflectionTestUtils.getField(persistedEntity, "password"));
            assertEquals(true, ReflectionTestUtils.getField(persistedEntity, "active"));
            verify(jwtService).generateToken(email);
        }

        @Test
        void given_existingEmail_when_registerUser_then_throwsEmailAlreadyExistsException() {
            var user = TestDataFactory.user();
            String email = "ana@example.com";

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
            String email = "ana@example.com";
            String rawPassword = "StrongPass1";
            String storedPassword = (String) ReflectionTestUtils.getField(userEntity, "password");
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
    }
}
