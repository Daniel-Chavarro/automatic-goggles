package org.java_avanzado.taller.controller.dto.request.update;

import lombok.Data;
import org.java_avanzado.taller.domain.model.OrderStatus;

@Data
public class UpdateOrderRequest {
    private OrderStatus status;
}