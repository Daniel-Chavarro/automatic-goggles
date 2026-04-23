package org.java_avanzado.taller.domain.exception;

/**
 * Exception thrown when attempting to authenticate with a disabled account.
 *
 * <p>This exception is used during login when the user account exists
 * but has been marked as inactive by an administrator.</p>
 */
public class InactiveUserException extends RuntimeException {
    public InactiveUserException(String message) {
        super(message);
    }
}
