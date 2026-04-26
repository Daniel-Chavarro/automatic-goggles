package org.java_avanzado.taller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Summary product information response")
public class ProductSummaryResponse {
     
    @Schema(description = "Unique identifier of the product", example = "1")
    private Long id;
    
    @Schema(description = "Product name", example = "Wireless Headphones")
    private String name;
    
    @Schema(description = "Product price", example = "89.99")
    private BigDecimal price;
}