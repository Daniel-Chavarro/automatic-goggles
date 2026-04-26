package org.java_avanzado.taller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshExpiredException extends RuntimeException {
    public TokenRefreshExpiredException(String message) {
        super(message);
    }
}