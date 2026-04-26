package org.java_avanzado.taller.domain.exception;

public class OrderFinishedExeption extends RuntimeException {
    public OrderFinishedExeption(String message) {
        super(message);
    }
}
