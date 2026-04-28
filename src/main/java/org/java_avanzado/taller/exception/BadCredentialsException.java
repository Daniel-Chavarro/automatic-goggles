package org.java_avanzado.taller.exception;

/**
 * Exception thrown when authentication credentials are invalid.
 *
 * <p>This exception is used during login when the provided email
 * does not exist or the password does not match the stored hash.</p>
 */
public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
