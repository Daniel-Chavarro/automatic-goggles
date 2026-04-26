package org.java_avanzado.taller.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    SUCCESSFUL_LOGIN("The user successfully logged in"),
    FAILED_LOGIN("The user failed to log in"),
    SUCCESSFUL_REGISTER("The user successfully registered"),
    FAILED_REGISTER("The user failed to register, already in use"),
    SUCCESSFUL_LOGOUT("The user successfully logged out"),
    FAILED_LOGOUT("The user failed to log out"),
    NO_AUTHORIZED_ACTION("The user attempted an unauthorized action");

    private final String description;
}
