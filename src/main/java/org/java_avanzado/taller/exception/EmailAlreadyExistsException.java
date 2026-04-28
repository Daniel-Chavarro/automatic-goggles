package org.java_avanzado.taller.exception;

/**
 * Exception thrown when attempting to create a user with an email that already exists.
 *
 * <p>This exception is used during user registration when the provided
 * email address is already associated with an existing account.</p>
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}