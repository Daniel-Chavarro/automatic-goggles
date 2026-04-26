package org.java_avanzado.taller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Schema(description = "Summary order information response")
public class OrderSummaryResponse {

    @Schema(description = "Unique identifier of the order", example = "1")
    private Long id;

    @Schema(description = "Total price of the order", example = "179.98")
    private BigDecimal totalPrice;

    @Schema(description = "Current status of the order")
    private OrderStatus status;

    @Schema(description = "Timestamp when the order was created", example = "2026-04-26T10:30:00Z")
    private Instant createdAt;
}