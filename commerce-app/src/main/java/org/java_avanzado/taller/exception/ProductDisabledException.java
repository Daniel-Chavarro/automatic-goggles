package org.java_avanzado.taller.exception;

/**
 * Exception thrown when attempting to add an inactive product to an order.
 *
 * <p>This exception is used when a product has been marked as inactive but
 * a user attempts to add it to their order. Only active products can be purchased.</p>
 */
public class ProductDisabledException extends RuntimeException {
    public ProductDisabledException(String message) {
        super(message);
    }
}
