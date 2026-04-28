package org.java_avanzado.taller.controller.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Payload for updating an existing product")
public class UpdateProductRequest {

    @Schema(description = "Product name", example = "Wireless Headphones Pro", maxLength = 255)
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;

    @Schema(description = "Product description", example = "Upgraded wireless headphones with improved battery life", maxLength = 1000)
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Schema(description = "Product price", example = "99.99")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Schema(description = "Available stock quantity", example = "75", minimum = "0")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}