package org.java_avanzado.taller.controller.dto.request.filter;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilterDto {
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean active;
}
