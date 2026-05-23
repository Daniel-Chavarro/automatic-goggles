package org.java_avanzado.taller.exception;

/**
 * Exception thrown when attempting to modify an order that has already been finalized.
 *
 * <p>This exception is used when operations like adding items or changing status
 * are attempted on an order that has already completed processing.</p>
 */
public class OrderAlreadyFinishedException extends RuntimeException {
    public OrderAlreadyFinishedException(String message) {
        super(message);
    }
}
