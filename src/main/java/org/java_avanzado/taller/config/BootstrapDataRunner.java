package org.java_avanzado.taller.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;
import org.java_avanzado.taller.domain.model.enums.UserRole;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.OrderProductEntity;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.OrderRepository;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootstrapDataRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder  passwordEncoder;

    @Value("${app.bootstrap.first-name}")
    private String firstName;

    @Value("${app.bootstrap.last-name}")
    private String lastName;

    @Value("${app.bootstrap.email}")
    private String email;

    @Value("${app.bootstrap.password}")
    private String password;

    @Value("${app.bootstrap.phone}")
    private String phone;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized, skipping bootstrap data");
            return;
        }

        log.info("Initializing bootstrap data...");

        UserEntity adminUser = createAdminUser();
        UserEntity regularUser = createRegularUser();
        
        List<ProductEntity> products = createProducts();
        
        createSampleOrders(adminUser, regularUser, products);

        log.info("Bootstrap data initialized successfully");
    }

    private UserEntity createAdminUser() {
        UserEntity user = UserEntity.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        
        return userRepository.save(user);
    }

    private UserEntity createRegularUser() {
        UserEntity user = UserEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .email("user@example.com")
                .password(passwordEncoder.encode("User123456!"))
                .phone("+0987654321")
                .role(UserRole.CLIENT)
                .active(true)
                .build();
        
        return userRepository.save(user);
    }

    private List<ProductEntity> createProducts() {
        ProductEntity coffee = ProductEntity.builder()
                .name("Coffee")
                .description("Fresh roasted coffee beans")
                .price(new BigDecimal("5.99"))
                .stockQuantity(100)
                .active(true)
                .version(0L)
                .build();

        ProductEntity tea = ProductEntity.builder()
                .name("Tea")
                .description("Premium green tea leaves")
                .price(new BigDecimal("3.99"))
                .stockQuantity(150)
                .active(true)
                .version(0L)
                .build();

        ProductEntity milk = ProductEntity.builder()
                .name("Milk")
                .description("Fresh whole milk")
                .price(new BigDecimal("2.49"))
                .stockQuantity(200)
                .active(true)
                .version(0L)
                .build();

        ProductEntity sugar = ProductEntity.builder()
                .name("Sugar")
                .description("White crystal sugar")
                .price(new BigDecimal("1.99"))
                .stockQuantity(300)
                .active(true)
                .version(0L)
                .build();

        ProductEntity cookies = ProductEntity.builder()
                .name("Cookies")
                .description("Chocolate chip cookies")
                .price(new BigDecimal("4.49"))
                .stockQuantity(80)
                .active(true)
                .version(0L)
                .build();

        return productRepository.saveAll(List.of(coffee, tea, milk, sugar, cookies));
    }

    private void createSampleOrders(UserEntity adminUser, UserEntity regularUser, List<ProductEntity> products) {
        ProductEntity coffee = products.get(0);
        ProductEntity tea = products.get(1);
        ProductEntity milk = products.get(2);

        int coffeeStockBefore = coffee.getStockQuantity();
        int teaStockBefore = tea.getStockQuantity();
        int milkStockBefore = milk.getStockQuantity();

        OrderEntity order1 = OrderEntity.builder()
                .user(adminUser)
                .orderStatus(OrderStatus.PENDING)
                .active(true)
                .version(0L)
                .orderProducts(new java.util.HashSet<>())
                .build();

        OrderProductEntity op1 = OrderProductEntity.builder()
                .order(order1)
                .product(coffee)
                .quantity(5)
                .unitPrice(coffee.getPrice())
                .build();
        order1.getOrderProducts().add(op1);

        OrderProductEntity op2 = OrderProductEntity.builder()
                .order(order1)
                .product(tea)
                .quantity(3)
                .unitPrice(tea.getPrice())
                .build();
        order1.getOrderProducts().add(op2);

        BigDecimal total1 = coffee.getPrice().multiply(BigDecimal.valueOf(5))
                .add(tea.getPrice().multiply(BigDecimal.valueOf(3)));
        order1.setTotalPrice(total1);

        coffee.setStockQuantity(coffeeStockBefore - 5);
        tea.setStockQuantity(teaStockBefore - 3);

        OrderEntity order2 = OrderEntity.builder()
                .user(regularUser)
                .orderStatus(OrderStatus.APPROVED)
                .active(true)
                .version(0L)
                .orderProducts(new java.util.HashSet<>())
                .build();

        OrderProductEntity op3 = OrderProductEntity.builder()
                .order(order2)
                .product(milk)
                .quantity(10)
                .unitPrice(milk.getPrice())
                .build();
        order2.getOrderProducts().add(op3);

        OrderProductEntity op4 = OrderProductEntity.builder()
                .order(order2)
                .product(tea)
                .quantity(2)
                .unitPrice(tea.getPrice())
                .build();
        order2.getOrderProducts().add(op4);

        BigDecimal total2 = milk.getPrice().multiply(BigDecimal.valueOf(10))
                .add(tea.getPrice().multiply(BigDecimal.valueOf(2)));
        order2.setTotalPrice(total2);

        milk.setStockQuantity(milkStockBefore - 10);
        tea.setStockQuantity(tea.getStockQuantity() - 2);

        productRepository.saveAll(products);
        orderRepository.saveAll(List.of(order1, order2));
    }
}