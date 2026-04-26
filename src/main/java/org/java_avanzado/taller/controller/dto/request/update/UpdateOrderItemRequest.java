package org.java_avanzado.taller.controller.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Represents the payload for updating an existing order item.
 */
@Data
@Schema(description = "Payload for updating the quantity of a product in an order")
public class UpdateOrderItemRequest {
    
    @Schema(description = "Unique identifier of the product to update", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long productId;
    
    @Schema(description = "New quantity for the product", example = "3", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1")
    @NotNull
    @Min(1)
    private int quantity;
}