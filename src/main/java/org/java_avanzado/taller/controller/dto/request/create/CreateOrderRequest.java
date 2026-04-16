package org.java_avanzado.taller.controller.dto.request.create;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<AddOrderItemRequest> items;
}
