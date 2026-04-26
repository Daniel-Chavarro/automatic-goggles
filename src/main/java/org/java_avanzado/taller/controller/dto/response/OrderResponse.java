package org.java_avanzado.taller.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed order information response")
public class OrderResponse {
     
    @Schema(description = "Unique identifier of the order", example = "1")
    private Long id;
    
    @Schema(description = "Unique identifier of the user who placed the order", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
    
    @Schema(description = "Total price of the order", example = "179.98")
    private BigDecimal totalPrice;
    
    @Schema(description = "Current status of the order")
    private OrderStatus status;
    
    @Schema(description = "Items in the order")
    private List<OrderItemResponse> items;
    
    @Schema(description = "Whether the order is active (not deleted)", example = "true")
    private boolean active;
    
    @Schema(description = "Timestamp when the order was created", example = "2026-04-26T10:30:00Z")
    private Instant createdAt;
}