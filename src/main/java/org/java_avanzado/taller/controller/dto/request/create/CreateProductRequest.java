package org.java_avanzado.taller.controller.dto.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Payload for creating a new product")
public class CreateProductRequest {
    
    @Schema(description = "Product name", example = "Wireless Headphones", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;
    
    @Schema(description = "Product description", example = "High-quality wireless headphones with noise cancellation", maxLength = 1000)
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @Schema(description = "Product price", example = "89.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @Schema(description = "Initial stock quantity", example = "50", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}