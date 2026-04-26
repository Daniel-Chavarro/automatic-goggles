package org.java_avanzado.taller.utils.validators;

import java.util.function.Consumer;

public class AuxiliaryMethods {

    /**
     * Auxiliary method to conditionally modify an entity field if the new value is not null.
     *
     * @param value the new value to set on the entity field
     * @param consumer the consumer to apply the new value
     * @param <T> the type of the entity field reference for the entity field to be updated
     */
    public static  <T> void modify(T value, Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }
}
