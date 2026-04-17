package org.java_avanzado.taller.controller;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.java_avanzado.taller.config.JwtAuthenticationFilter;
import org.java_avanzado.taller.controller.dto.request.create.CreateOrderRequest;
import org.java_avanzado.taller.controller.dto.response.OrderItemResponse;
import org.java_avanzado.taller.controller.dto.response.OrderResponse;
import org.java_avanzado.taller.domain.model.OrderStatus;
import org.java_avanzado.taller.service.OrderService;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.utils.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private OrderMapper orderMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void given_validRequest_when_createOrderForUser_then_returns201AndOrderPayload() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var product = TestDataFactory.product();
        var order = TestDataFactory.order();
        var response = OrderResponse.builder()
                .id(9L)
                .userId(userId)
                .totalPrice(new BigDecimal("25.00"))
                .status(OrderStatus.APPROVED)
                .items(List.of(OrderItemResponse.builder()
                        .productId(1L)
                        .productName("Coffee")
                        .quantity(2)
                        .unitPrice(new BigDecimal("12.50"))
                        .build()))
                .active(true)
                .build();

        when(productService.getActiveProduct(1L)).thenReturn(product);
        when(orderService.createOrder(eq(userId), any(CreateOrderRequest.class))).thenReturn(order);
        when(orderMapper.fromOrderToResponse(eq(order), anyMap())).thenReturn(response);

        String requestBody = """
                {
                  \"items\": [
                    {
                      \"productId\": 1,
                      \"quantity\": 2
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/orders/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.userId").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.totalPrice").value(25.00))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].productName").value("Coffee"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(12.50));

        ArgumentCaptor<Map<String, String>> productNameMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(orderMapper).fromOrderToResponse(eq(order), productNameMapCaptor.capture());
        verify(productService).getActiveProduct(1L);
        assertEquals("Coffee", productNameMapCaptor.getValue().get("1"));
    }

    @Test
    void given_invalidRequest_when_createOrderForUser_then_returns400() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        String invalidRequestBody = """
                {
                  \"items\": []
                }
                """;

        mockMvc.perform(post("/api/orders/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService, productService, orderMapper);
    }
}
