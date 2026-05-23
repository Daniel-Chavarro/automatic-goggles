package org.java_avanzado.taller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Individual item within an order")
public record OrderItemResponse(
        @Schema(description = "Unique identifier of the product", example = "1") Long productId,
        @Schema(description = "Name of the product", example = "Wireless Headphones") String productName,
        @Schema(description = "Quantity of the product in the order", example = "2") Integer quantity,
        @Schema(description = "Price per unit of the product", example = "89.99") BigDecimal unitPrice
) {
}