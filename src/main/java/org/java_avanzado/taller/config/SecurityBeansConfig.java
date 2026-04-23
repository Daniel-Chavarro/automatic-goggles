package org.java_avanzado.taller.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security bean configurations.
 *
 * <p>Provides password encoding and other security-related beans.</p>
 */
@Configuration
public class SecurityBeansConfig {
    /**
     * Creates a BCrypt password encoder for credential hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}