package org.java_avanzado.taller.controller.dto.request.update;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Represents the payload for updating an existing order item.
 */
@Data
public class UpdateOrderItemRequest {
    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private int quantity;
}
