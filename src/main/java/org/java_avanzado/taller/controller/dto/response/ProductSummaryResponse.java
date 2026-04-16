package org.java_avanzado.taller.controller.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductSummaryResponse {
    
    private Long id;
    private String name;
    private BigDecimal price;
}