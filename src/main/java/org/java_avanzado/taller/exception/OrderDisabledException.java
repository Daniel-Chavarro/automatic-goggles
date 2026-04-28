package org.java_avanzado.taller.exception;

public class OrderDisabledException extends RuntimeException {
    public OrderDisabledException(String message) {
        super(message);
    }
}
