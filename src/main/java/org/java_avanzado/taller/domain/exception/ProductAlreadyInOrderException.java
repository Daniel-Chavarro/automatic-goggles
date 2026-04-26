package org.java_avanzado.taller.domain.exception;

public class ProductAlreadyInOrderException extends RuntimeException {
    public ProductAlreadyInOrderException(String message) {
        super(message);
    }
}
