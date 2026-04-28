package org.java_avanzado.taller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Summary product information response")
public record ProductSummaryResponse(
        @Schema(description = "Unique identifier of the product", example = "1") Long id,
        @Schema(description = "Product name", example = "Wireless Headphones") String name,
        @Schema(description = "Product price", example = "89.99") BigDecimal price
) {
}