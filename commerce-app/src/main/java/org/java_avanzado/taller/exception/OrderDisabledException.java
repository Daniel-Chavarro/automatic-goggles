package org.java_avanzado.taller.exception;

/**
 * Exception thrown when attempting to create or modify an order for a disabled user.
 *
 * <p>This exception is used when an inactive user attempts to create or interact
 * with orders in the system.</p>
 */
public class OrderDisabledException extends RuntimeException {
    public OrderDisabledException(String message) {
        super(message);
    }
}
