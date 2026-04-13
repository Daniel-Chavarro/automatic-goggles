package org.java_avanzado.taller_1_java.persistence.domain.model;

import lombok.Builder;
import lombok.Data;
import org.java_avanzado.taller_1_java.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Data
public class Order {
    private Long id;
    private UUID userId;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    @Builder.Default
    private List<OrderProduct> orderProducts = new ArrayList<>();
    private boolean active;

}
