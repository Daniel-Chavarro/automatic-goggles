package org.java_avanzado.taller.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed product information response")
public class ProductResponse {
     
    @Schema(description = "Unique identifier of the product", example = "1")
    private Long id;
    
    @Schema(description = "Product name", example = "Wireless Headphones")
    private String name;
    
    @Schema(description = "Product description", example = "High-quality wireless headphones with noise cancellation")
    private String description;
    
    @Schema(description = "Product price", example = "89.99")
    private BigDecimal price;
    
    @Schema(description = "Available stock quantity", example = "50")
    private Integer stock;
    
    @Schema(description = "Whether the product is active and available for purchase", example = "true")
    private boolean active;
    
    @Schema(description = "Timestamp when the product was created", example = "2026-04-26T10:30:00Z")
    private Instant createdAt;
}