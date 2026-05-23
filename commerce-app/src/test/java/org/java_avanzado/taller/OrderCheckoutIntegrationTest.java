package org.java_avanzado.taller;

import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;
import org.java_avanzado.taller.domain.model.enums.UserRole;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.OrderRepository;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.rabbitmq.dynamic=false")
class OrderCheckoutIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void checkoutMovesOrderToPaymentPendingAndRejectsFurtherItemMutation() throws Exception {
        ProductEntity product = createProduct("checkout-freeze-product");
        Order order = createPendingOrderWithItem(product, 1);

        mockMvc.perform(post("/api/orders/{id}/checkout", order.getId())
                        .with(user("checkout-client").roles("CLIENT")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value(order.getId()))
                .andExpect(jsonPath("$.status").value("PAYMENT_PENDING"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());

        assertThat(orderRepository.findById(order.getId()).orElseThrow().getOrderStatus())
                .isEqualTo(OrderStatus.PAYMENT_PENDING);

        mockMvc.perform(put("/api/orders/{id}/items", order.getId())
                        .with(user("checkout-client").roles("CLIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + product.getId() + ",\"quantity\":2}"))
                .andExpect(status().isConflict());
    }

    @Test
    void emptyOrderCheckoutFailsWithoutPublishingPaymentRequest() throws Exception {
        Order order = orderService.createOrder(createUser().getId());

        mockMvc.perform(post("/api/orders/{id}/checkout", order.getId())
                        .with(user("checkout-client").roles("CLIENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot checkout an order without items"));

        assertThat(orderRepository.findById(order.getId()).orElseThrow().getOrderStatus())
                .isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void duplicateCheckoutFailsAfterOrderIsPaymentPending() throws Exception {
        Order order = createPendingOrderWithItem(createProduct("duplicate-checkout-product"), 1);

        mockMvc.perform(post("/api/orders/{id}/checkout", order.getId())
                        .with(user("checkout-client").roles("CLIENT")))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/orders/{id}/checkout", order.getId())
                        .with(user("checkout-client").roles("CLIENT")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot checkout order, order status is PAYMENT_PENDING"));
    }

    @Test
    void terminalOrderCheckoutFails() throws Exception {
        Order order = createPendingOrderWithItem(createProduct("terminal-checkout-product"), 1);
        orderService.modifyOrderStatus(order.getId(), OrderStatus.APPROVED);

        mockMvc.perform(post("/api/orders/{id}/checkout", order.getId())
                        .with(user("checkout-client").roles("CLIENT")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot checkout order, order status is APPROVED"));
    }

    @Test
    void checkoutRejectsOrderWithNonPositiveTotal() throws Exception {
        ProductEntity product = createProduct("zero-total-product");
        Order order = createPendingOrderWithItem(product, 1);
        orderRepository.findById(order.getId()).ifPresent(entity -> {
            entity.setTotalPrice(BigDecimal.ZERO);
            orderRepository.save(entity);
        });

        mockMvc.perform(post("/api/orders/{id}/checkout", order.getId())
                        .with(user("checkout-client").roles("CLIENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot checkout an order with a non-positive total"));
    }

    private Order createPendingOrderWithItem(ProductEntity product, int quantity) {
        Order order = orderService.createOrder(createUser().getId());
        return orderService.addProductToOrder(order.getId(), product.getId(), quantity);
    }

    private UserEntity createUser() {
        String suffix = UUID.randomUUID().toString();
        return userRepository.save(UserEntity.builder()
                .firstName("Checkout")
                .lastName("Client")
                .email("checkout-" + suffix + "@example.com")
                .password("password")
                .phone("1234567890")
                .role(UserRole.CLIENT)
                .active(true)
                .build());
    }

    private ProductEntity createProduct(String name) {
        return productRepository.save(ProductEntity.builder()
                .name(name + "-" + UUID.randomUUID())
                .description("Checkout test product")
                .price(new BigDecimal("25.00"))
                .stockQuantity(10)
                .active(true)
                .build());
    }
}
