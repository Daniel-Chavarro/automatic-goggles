package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.config.JwtService;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.domain.model.UserRole;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.utils.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public String registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        UserEntity entity = userMapper.toEntity(user);
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        entity.setActive(true);
        if (entity.getRole() == null) {
            entity.setRole(UserRole.CLIENT);
        }
        userRepository.save(entity);
        return jwtService.generateToken(entity.getEmail());
    }

    public String authenticate(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!user.isActive()) {
            throw new RuntimeException("Account disabled");
        }
        return jwtService.generateToken(email);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).map(userMapper::toDomain)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void disableUser(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        entity.setActive(false);
        userRepository.save(entity);
    }
}