package org.java_avanzado.taller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Summary order information response")
public record OrderSummaryResponse(
        @Schema(description = "Unique identifier of the order", example = "1") Long id,
        @Schema(description = "Total price of the order", example = "179.98") BigDecimal totalPrice,
        @Schema(description = "Current status of the order") OrderStatus status,
        @Schema(description = "Timestamp when the order was created", example = "2026-04-26T10:30:00Z") Instant createdAt
) {
}