package org.java_avanzado.taller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Individual item within an order")
public class OrderItemResponse {
     
    @Schema(description = "Unique identifier of the product", example = "1")
    private Long productId;
    
    @Schema(description = "Name of the product", example = "Wireless Headphones")
    private String productName;
    
    @Schema(description = "Quantity of the product in the order", example = "2")
    private Integer quantity;
    
    @Schema(description = "Price per unit of the product", example = "89.99")
    private BigDecimal unitPrice;
}