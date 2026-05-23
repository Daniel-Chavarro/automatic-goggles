package org.java_avanzado.taller.exception;

/**
 * Exception thrown when a requested order cannot be found.
 *
 * <p>This exception is used when order lookup operations fail
 * to locate an order with the specified identifier.</p>
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
