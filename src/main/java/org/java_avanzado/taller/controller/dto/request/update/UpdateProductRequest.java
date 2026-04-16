package org.java_avanzado.taller.controller.dto.request.update;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {
    
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}