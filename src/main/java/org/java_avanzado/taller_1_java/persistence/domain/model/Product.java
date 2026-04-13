package org.java_avanzado.taller_1_java.persistence.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;
    private boolean active;
}
