package org.java_avanzado.taller.exception;

public class ProductAlreadyInOrderException extends RuntimeException {
    public ProductAlreadyInOrderException(String message) {
        super(message);
    }
}
