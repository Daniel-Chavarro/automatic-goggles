package org.java_avanzado.taller.exception;

/**
 * Exception thrown when a requested product cannot be found.
 *
 * <p>This exception is used when product lookup operations fail
 * to locate a product with the specified identifier.</p>
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
