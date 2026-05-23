package org.java_avanzado.taller.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuration for JPA auditing support.
 *
 * <p>This configuration enables automatic population of
 * {@link org.springframework.data.annotation.CreatedBy} and
 * {@link org.springframework.data.annotation.LastModifiedBy} fields
 * on JPA entities that extend {@link org.java_avanzado.taller.persistence.entity.AuditableEntity}.</p>
 *
 * <p>The auditor provider returns the current authenticated username
 * when available, or falls back to "system" for programmatic operations
 * and startup initializers.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }
            return Optional.ofNullable(authentication.getName()).filter(name -> !name.isBlank()).or(() -> Optional.of("system"));
        };
    }
}
