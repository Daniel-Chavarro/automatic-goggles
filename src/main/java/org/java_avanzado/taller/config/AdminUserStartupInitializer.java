package org.java_avanzado.taller.config;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.model.UserRole;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the default administrator account on first application startup.
 *
 * <p>This runner ensures that an admin user exists for system management
 * purposes. It is executed once during application startup and can be
 * controlled via the following properties:</p>
 * <ul>
 *   <li>{@code app.bootstrap.admin.enabled} - Enable/disable bootstrap</li>
 *   <li>{@code app.bootstrap.admin.email} - Admin email address</li>
 *   <li>{@code app.bootstrap.admin.password} - Admin password</li>
 * </ul>
 *
 * <p>The bootstrap is skipped if a user with the configured email
 * already exists in the database.</p>
 */
@Component
@RequiredArgsConstructor
public class AdminUserStartupInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserStartupInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:true}")
    private boolean enabled;

    @Value("${app.bootstrap.admin.email:admin@local.dev}")
    private String email;

    @Value("${app.bootstrap.admin.password:Admin123!}")
    private String password;

    @Value("${app.bootstrap.admin.first-name:System}")
    private String firstName;

    @Value("${app.bootstrap.admin.last-name:Administrator}")
    private String lastName;

    @Value("${app.bootstrap.admin.phone:3000000000}")
    private String phone;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            logger.warn("Skipping admin bootstrap: email/password are empty");
            return;
        }

        if (userRepository.findByEmail(email).isPresent()) {
            logger.info("Admin bootstrap skipped: user with email {} already exists", email);
            return;
        }

        UserEntity adminUser = UserEntity.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        userRepository.save(adminUser);
        logger.info("Default admin user created for email {}", email);
    }
}
