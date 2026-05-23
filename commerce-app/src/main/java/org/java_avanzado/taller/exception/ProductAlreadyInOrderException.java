package org.java_avanzado.taller.exception;

/**
 * Exception thrown when attempting to add a product to an order where it already exists.
 *
 * <p>This exception is used when a user tries to add a product that is already
 * present in the order. The quantity should be updated instead of adding again.</p>
 */
public class ProductAlreadyInOrderException extends RuntimeException {
    public ProductAlreadyInOrderException(String message) {
        super(message);
    }
}
