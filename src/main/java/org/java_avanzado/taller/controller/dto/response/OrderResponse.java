package org.java_avanzado.taller.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed order information response")
public record OrderResponse(
        @Schema(description = "Unique identifier of the order", example = "1") Long id,
        @Schema(description = "Unique identifier of the user who placed the order", example = "123e4567-e89b-12d3-a456-426614174000") UUID userId,
        @Schema(description = "Total price of the order", example = "179.98") BigDecimal totalPrice,
        @Schema(description = "Current status of the order") OrderStatus status,
        @Schema(description = "Items in the order") Set<OrderItemResponse> items,
        @Schema(description = "Whether the order is active (not deleted)", example = "true") boolean active,
        @Schema(description = "Timestamp when the order was created", example = "2026-04-26T10:30:00Z") Instant createdAt
) {
}