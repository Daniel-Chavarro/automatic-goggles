package org.java_avanzado.taller.config;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;
import org.java_avanzado.taller.domain.model.enums.UserRole;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.OrderProductEntity;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.OrderRepository;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes demo data on application startup for development and testing.
 *
 * <p>This runner creates sample entities when the application starts in
 * development mode, including:</p>
 * <ul>
 *   <li>A demo client user account</li>
 *   <li>Sample products for the coffee shop catalog</li>
 *   <li>A sample pending order with items</li>
 * </ul>
 *
 * <p>Execution is controlled by the {@code app.bootstrap.demo-data.enabled}
 * property and is skipped when data already exists.</p>
 */
@Component
@RequiredArgsConstructor
public class DemoDataStartupInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DemoDataStartupInitializer.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.demo-data.enabled:true}")
    private boolean enabled;

    @Value("${app.bootstrap.demo-data.user-email:client@local.dev}")
    private String demoUserEmail;

    @Value("${app.bootstrap.demo-data.user-password:Client123!}")
    private String demoUserPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        UserEntity demoUser = ensureDemoUser();
        List<ProductEntity> products = ensureDemoProducts();
        ensureDemoOrder(demoUser, products);
    }

    private UserEntity ensureDemoUser() {
        return userRepository.findByEmail(demoUserEmail).orElseGet(() -> {
            UserEntity user = UserEntity.builder()
                    .firstName("Demo")
                    .lastName("Client")
                    .email(demoUserEmail)
                    .phone("3012345678")
                    .password(passwordEncoder.encode(demoUserPassword))
                    .role(UserRole.CLIENT)
                    .active(true)
                    .build();
            UserEntity saved = userRepository.save(user);
            logger.info("Demo user created with email {}", demoUserEmail);
            return saved;
        });
    }

    private List<ProductEntity> ensureDemoProducts() {
        if (productRepository.count() > 0) {
            return productRepository.findAll();
        }

        List<ProductEntity> products = List.of(
                ProductEntity.builder().name("Coffee Beans 500g").description("Medium roast Arabica beans").price(new BigDecimal("12.50")).stockQuantity(100).active(true).build(),
                ProductEntity.builder().name("French Press 1L").description("Glass and stainless steel French press").price(new BigDecimal("24.90")).stockQuantity(25).active(true).build(),
                ProductEntity.builder().name("Ceramic Mug").description("350ml ceramic mug").price(new BigDecimal("7.20")).stockQuantity(80).active(true).build(),
                ProductEntity.builder().name("Drip Filter Pack").description("Pack of 100 paper filters").price(new BigDecimal("5.60")).stockQuantity(60).active(true).build()
        );

        List<ProductEntity> saved = productRepository.saveAll(products);
        logger.info("Demo products created: {}", saved.size());
        return saved;
    }

    private void ensureDemoOrder(UserEntity user, List<ProductEntity> products) {
        if (orderRepository.count() > 0 || products.size() < 2) {
            return;
        }

        ProductEntity firstProduct = products.get(0);
        ProductEntity secondProduct = products.get(1);

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(firstProduct.getPrice().multiply(BigDecimal.valueOf(2)).add(secondProduct.getPrice()))
                .build();

        List<OrderProductEntity> orderItems = new ArrayList<>();
        orderItems.add(OrderProductEntity.builder().order(order).product(firstProduct).quantity(2).unitPrice(firstProduct.getPrice()).build());
        orderItems.add(OrderProductEntity.builder().order(order).product(secondProduct).quantity(1).unitPrice(secondProduct.getPrice()).build());
        order.setOrderProducts(orderItems);

        orderRepository.save(order);
        logger.info("Demo order created for user {}", user.getEmail());
    }
}
