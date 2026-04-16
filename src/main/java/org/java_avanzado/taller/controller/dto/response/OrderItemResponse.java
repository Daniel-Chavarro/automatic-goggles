package org.java_avanzado.taller.controller.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}
