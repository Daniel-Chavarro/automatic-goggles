package org.java_avanzado.taller.exception;

public class OrderAlreadyFinishedException extends RuntimeException {
    public OrderAlreadyFinishedException(String message) {
        super(message);
    }
}
