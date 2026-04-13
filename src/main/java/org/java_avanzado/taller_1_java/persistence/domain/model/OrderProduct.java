package org.java_avanzado.taller_1_java.persistence.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderProduct {
    private Long id;
    private String productId;
    private Integer quantity;
    private BigDecimal unitPrice;
}
