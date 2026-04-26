package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.auth.LoginUserRequest;
import org.java_avanzado.taller.controller.dto.request.auth.RegisterUserRequest;
import org.java_avanzado.taller.controller.dto.response.JwtAuthResponse;
import org.java_avanzado.taller.domain.exception.EmailAlreadyExistsException;
import org.java_avanzado.taller.domain.exception.UserNotFoundException;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.domain.model.enums.EventType;
import org.java_avanzado.taller.domain.model.enums.UserRole;
import org.java_avanzado.taller.persistence.entity.RefreshTokenEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.security.CustomUserDetails;
import org.java_avanzado.taller.security.CustomUserDetailsService;
import org.java_avanzado.taller.utils.mapper.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EventLogService eventLogService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Transactional
    public JwtAuthResponse register(RegisterUserRequest request) {
        try{
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new EmailAlreadyExistsException("Email already in use");
            }

            UserEntity user = UserEntity.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .phone(request.getPhone())
                    .role(UserRole.CLIENT)
                    .active(true)
                    .build();

            user = userRepository.save(user);

            User domainUser = userMapper.fromUserEntityToDomain(user);
            CustomUserDetails userDetails = new CustomUserDetails(domainUser);
            String accessToken = jwtService.generateToken(userDetails);

            String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

            return JwtAuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (Exception e){
            eventLogService.logEvent(EventType.FAILED_REGISTER, request.getEmail());
            throw e;
        }
    }

    @Transactional
    public JwtAuthResponse login(LoginUserRequest request) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            UserEntity user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userDetails.getId()));

            User domainUser = userMapper.fromUserEntityToDomain(user);
            CustomUserDetails customUserDetails = new CustomUserDetails(domainUser);
            String accessToken = jwtService.generateToken(customUserDetails);

            String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

            return JwtAuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (Exception e){
            eventLogService.logEvent(EventType.FAILED_LOGIN, request.getEmail());
            throw e;
        }
    }

    public JwtAuthResponse refresh(String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(refreshTokenEntity);

        String email = refreshTokenEntity.getUser().getEmail();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        User domainUser = userMapper.fromUserEntityToDomain(user);
        CustomUserDetails userDetails = new CustomUserDetails(domainUser);
        String accessToken = jwtService.generateToken(userDetails);

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        try {
            refreshTokenService.deleteByToken(refreshToken);
        } catch (Exception e) {
            eventLogService.logEvent(EventType.FAILED_LOGOUT, "Unknown email");
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }
}