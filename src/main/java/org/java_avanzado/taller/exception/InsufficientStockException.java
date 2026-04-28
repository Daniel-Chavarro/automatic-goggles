package org.java_avanzado.taller.exception;

/**
 * Exception thrown when there is insufficient stock to fulfill an order.
 *
 * <p>This exception is used when attempting to create or update an order
 * and the requested quantity exceeds available stock.</p>
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
