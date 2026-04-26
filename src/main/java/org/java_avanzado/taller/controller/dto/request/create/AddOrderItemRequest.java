package org.java_avanzado.taller.controller.dto.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Represents the payload for adding an item to an existing order.
 */
@Data
@Schema(description = "Payload for adding a product to an order")
public class AddOrderItemRequest {
    
    @Schema(description = "Unique identifier of the product to add", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @Schema(description = "Quantity of the product to add", example = "2", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}