package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.exception.BadCredentialsException;
import org.java_avanzado.taller.domain.exception.EmailAlreadyExistsException;
import org.java_avanzado.taller.domain.exception.InactiveUserException;
import org.java_avanzado.taller.domain.exception.UserNotFoundException;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.domain.model.enums.UserRole;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.utils.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.java_avanzado.taller.controller.dto.request.filter.UserFilterDto;
import org.java_avanzado.taller.persistence.specification.UserSpecifications;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.java_avanzado.taller.utils.validators.AuxiliaryMethods.modify;

/**
 * Service for user management operations.
 *
 * <p>Handles user registration, authentication, retrieval,
 * and administrative updates.</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers a new user with the provided credentials.
     *
     * @param user the user domain model containing registration data
     * @return a JWT token for the newly registered user
     * @throws IllegalArgumentException    if user, email, or password is missing
     * @throws EmailAlreadyExistsException if the email is already registered
     */
    @Deprecated(forRemoval = true)
    @Transactional
    public String registerUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already in use");
        }
        UserEntity entity = userMapper.fromUserToEntity(user);
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        entity.setActive(true);
        if (entity.getRole() == null) {
            entity.setRole(UserRole.CLIENT);
        }
        userRepository.save(entity);
        return jwtService.generateToken(entity.getEmail());
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param email    the user's email address
     * @param password the user's plain-text password
     * @return a JWT token if authentication succeeds
     * @throws BadCredentialsException if email not found or password mismatch
     * @throws InactiveUserException   if the account is disabled
     */
    @Transactional(readOnly = true)
    @Deprecated(forRemoval = true)
    public String authenticate(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!user.isActive()) {
            throw new InactiveUserException("Account disabled");
        }
        return jwtService.generateToken(email);
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the user's UUID
     * @return the user domain model
     * @throws UserNotFoundException if no user exists with the given ID
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id).map(userMapper::fromUserEntityToDomain)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the user's email
     * @return the user domain model
     * @throws UserNotFoundException if no user exists with the given email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::fromUserEntityToDomain)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    /**
     * Retrieves a paginated list of users based on the provided filter.
     *
     * @param filter   filter criteria
     * @param pageable pagination and sorting information
     * @return a page of users matching the filter
     */
    @Transactional(readOnly = true)
    public Page<User> getUsers(UserFilterDto filter, Pageable pageable) {
        Specification<UserEntity> spec = UserSpecifications.withFilter(filter);
        Page<UserEntity> entityPage = userRepository.findAll(spec, pageable);
        return entityPage.map(userMapper::fromUserEntityToDomain);
    }

    /**
     * Retrieves all registered users.
     *
     * @return a list of all user domain models
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::fromUserEntityToDomain)
                .collect(Collectors.toList());
    }

    /**
     * Disables a user account, preventing login.
     *
     * @param id the user's UUID to disable
     * @throws UserNotFoundException if no user exists with the given ID
     */
    @Transactional
    public void disableUser(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        entity.setActive(false);
        userRepository.save(entity);
    }

    /**
     * Creates a new user account with the provided data.
     *
     * @param user the user domain model
     * @return the created user domain model
     */
    @Transactional
    public User createUser(User user) {
        validateUser(user);

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        UserEntity entity = userMapper.fromUserToEntity(user);
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.fromUserEntityToDomain(userRepository.save(entity));
    }

    /**
     * Updates mutable fields of an existing user account.
     *
     * @param id   the user's UUID
     * @param data the updated user data
     * @return the updated user domain model
     */
    @Transactional
    public User updateUser(UUID id, User data) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        modify(data.getFirstName(), entity::setFirstName);
        modify(data.getLastName(), entity::setLastName);

        if (userRepository.findByEmail(data.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        modify(data.getEmail(), entity::setEmail);

        // Future: update password using Baeldung

        return userMapper.fromUserEntityToDomain(userRepository.save(entity));
    }

    /**
     * Auxiliary method to validate user data before creation.
     *
     * @param user the user domain model to validate
     * @throws IllegalArgumentException if any required field is missing or blank
     */
    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (user.getLastName() == null || user.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (user.getPhone() == null || user.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
    }
}
