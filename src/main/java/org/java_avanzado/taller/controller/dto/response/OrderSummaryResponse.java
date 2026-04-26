package org.java_avanzado.taller.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderSummaryResponse {
    
    private Long id;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private Instant createdAt;
}
