package org.java_avanzado.taller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;

import java.util.UUID;

@Schema(description = "Order checkout response")
public record OrderCheckoutResponse(
        @Schema(description = "Unique identifier of the order", example = "1") Long orderId,
        @Schema(description = "Current status of the order") OrderStatus status,
        @Schema(description = "Checkout correlation identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID correlationId
) {
}
