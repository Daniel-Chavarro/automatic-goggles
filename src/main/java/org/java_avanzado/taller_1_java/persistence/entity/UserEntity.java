package org.java_avanzado.taller_1_java.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.java_avanzado.taller_1_java.domain.model.UserRole;

import java.util.UUID;

/**
 * Entity that represents a system user.
 *
 * <p>Stores identity, contact, and authorization data through the user's
 * functional role.</p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity extends AuditableEntity {

    /** Unique technical identifier of the user. */
    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** User first name. */
    @Column(name="first_name", nullable = false)
    private String firstName;

    /** User last name. */
    @Column(name="last_name", nullable = false)
    private String lastName;

    /** Primary email, unique within the system. */
    @Column(name="email", nullable = false, unique = true)
    private String email;

    /** Optional contact phone number. */
    @Column(name="phone")
    private String phone;

    /** Persisted hash or credential used for authentication. */
    @Column(name="password", nullable = false)
    private String password;

    /** Role that defines permissions and functional scope. */
    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private UserRole role;

    /** Logical activation status of the account. */
    @Column(name = "active", nullable = false)
    private boolean active;
}
