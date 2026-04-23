package org.java_avanzado.taller.domain.exception;

/**
 * Exception thrown when a requested user cannot be found.
 *
 * <p>This exception is used when user lookup operations fail to
 * locate a user with the specified identifier or email.</p>
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}