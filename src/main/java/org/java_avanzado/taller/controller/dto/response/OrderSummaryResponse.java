package org.java_avanzado.taller.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderSummaryResponse {
    
    private Long id;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
