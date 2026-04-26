package org.java_avanzado.taller.controller.dto.request.filter;

import lombok.Data;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderFilterDto {
    private BigDecimal minTotal;
    private BigDecimal maxTotal;
    private OrderStatus status;
    private Boolean active;
    private UUID userId;
}
