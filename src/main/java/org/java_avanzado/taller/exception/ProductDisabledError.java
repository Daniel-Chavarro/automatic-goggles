package org.java_avanzado.taller.exception;

public class ProductDisabledError extends RuntimeException {
    public ProductDisabledError(String message) {
        super(message);
    }
}
