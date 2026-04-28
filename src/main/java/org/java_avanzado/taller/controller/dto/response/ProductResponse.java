package org.java_avanzado.taller.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed product information response")
public record ProductResponse(
        @Schema(description = "Unique identifier of the product", example = "1") Long id,
        @Schema(description = "Product name", example = "Wireless Headphones") String name,
        @Schema(description = "Product description", example = "High-quality wireless headphones with noise cancellation") String description,
        @Schema(description = "Product price", example = "89.99") BigDecimal price,
        @Schema(description = "Available stock quantity", example = "50") Integer stock,
        @Schema(description = "Whether the product is active and available for purchase", example = "true") boolean active,
        @Schema(description = "Timestamp when the product was created", example = "2026-04-26T10:30:00Z") Instant createdAt
) {
}