package org.java_avanzado.taller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a refresh token has expired or is invalid.
 *
 * <p>This exception is thrown during token refresh operations when the provided
 * refresh token is no longer valid. Users must log in again to obtain new tokens.</p>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshExpiredException extends RuntimeException {
    public TokenRefreshExpiredException(String message) {
        super(message);
    }
}