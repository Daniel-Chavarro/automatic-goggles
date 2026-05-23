package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.filter.OrderFilterDto;
import org.java_avanzado.taller.exception.OrderDisabledException;
import org.java_avanzado.taller.exception.OrderNotFoundException;
import org.java_avanzado.taller.exception.ProductDisabledException;
import org.java_avanzado.taller.exception.ProductNotFoundException;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.repository.OrderRepository;
import org.java_avanzado.taller.persistence.specification.OrderSpecifications;
import org.java_avanzado.taller.utils.mapper.OrderMapper;
import org.java_avanzado.taller.utils.mapper.ReferenceMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Service for order lifecycle operations.
 *
 * <p>Handles order creation, item mutations, and retrieval queries while
 * coordinating stock updates through the product service.</p>
 */
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductService productService;
    private final ReferenceMapper referenceMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Creates a new order for the specified user.
     * The order is initialized with an empty product list and a total of zero.
     *
     * @param userId the identifier of the user for whom to create the order
     * @return the created order
     */
    @Transactional
    public Order createOrder(UUID userId) {
        Order order = Order.builder()
                .userId(userId)
                .build();
        OrderEntity savedEntity = orderRepository.save(orderMapper.fromOrderToEntity(order));
        return orderMapper.fromOrderEntityToDomain(savedEntity);
    }

    /**
     * Adds a product to an existing order, updating the order total and product stock accordingly.
     *
     * @param orderId   the identifier of the order to which the product will be added
     * @param productId the identifier of the product to add
     * @param quantity  the quantity of the product to add
     * @return the updated order
     * @throws OrderNotFoundException   if no order exists with the provided identifier
     * @throws ProductNotFoundException if no active product exists with the provided identifier
     */
    @Transactional
    public Order addProductToOrder(Long orderId, Long productId, int quantity) {
        Order order = getOrderById(orderId);
        Product product = productService.getActiveProduct(productId);

        if (!product.isActive()) {
            throw new ProductDisabledException("Order already finished (COMPLETED/CANCELLED)");
        }

        order.addProduct(product, quantity);
        productService.updateProduct(productId, product);
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        // update the JPA entity from the domain model, providing ReferenceMapper context
        orderMapper.updateEntityFromDomain(order, entity, referenceMapper);
        OrderEntity updatedEntity = orderRepository.save(entity);
        return orderMapper.fromOrderEntityToDomain(updatedEntity);
    }

    /**
     * Removes a product from an existing order, updating the order total and product stock accordingly.
     *
     * @param orderId   the identifier of the order from which to remove the product
     * @param productId the identifier of the product to remove
     * @return the updated order
     * @throws OrderNotFoundException   if no order exists with the provided identifier
     * @throws ProductNotFoundException if no active product exists with the provided identifier
     */
    @Transactional
    public Order removeProductFromOrder(Long orderId, Long productId) {
        Order order = getOrderById(orderId);

        if (!order.isActive()){
            throw new OrderDisabledException("Order has been disabled");
        }

        Product product = productService.getActiveProduct(productId);

        if (!product.isActive()) {
            throw new ProductDisabledException("Product is disabled");
        }

        order.removeProduct(product);
        productService.updateProduct(productId, product);
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        orderMapper.updateEntityFromDomain(order, entity, referenceMapper);
        OrderEntity updatedEntity = orderRepository.save(entity);
        return orderMapper.fromOrderEntityToDomain(updatedEntity);
    }

    /**
     * Modifies the quantity of a product in an existing order, updating the order total and product stock accordingly.
     *
     * @param orderId   the identifier of the order in which to modify the product quantity
     * @param productId the identifier of the product for which to modify the quantity
     * @param quantity  the new quantity of the product in the order
     * @return the updated order
     * @throws OrderNotFoundException   if no order exists with the provided identifier
     * @throws ProductNotFoundException if no active product exists with the provided identifier
     */
    @Transactional
    public Order modifyQuantityProductInOrder(Long orderId, Long productId, int quantity) {
        Order order = getOrderById(orderId);

        if (!order.isActive()){
            throw new OrderDisabledException("Order has been disabled");
        }

        Product product = productService.getActiveProduct(productId);

        if (!product.isActive()) {
            throw new ProductDisabledException("Product is disabled");
        }

        order.updateQuantity(product, quantity);
        productService.updateProduct(productId, product);
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        orderMapper.updateEntityFromDomain(order, entity, referenceMapper);
        OrderEntity updatedEntity = orderRepository.save(entity);
        return orderMapper.fromOrderEntityToDomain(updatedEntity);
    }

    /**
     * Retrieves a paginated list of orders based on the provided filter.
     *
     * @param filter   filter criteria
     * @param pageable pagination and sorting information
     * @return a page of orders matching the filter
     */
    @Transactional(readOnly = true)
    public Page<Order> getOrders(OrderFilterDto filter, Pageable pageable) {
        Specification<OrderEntity> spec = OrderSpecifications.withFilter(filter);
        Page<OrderEntity> entityPage = orderRepository.findAll(spec, pageable);
        return entityPage.map(orderMapper::fromOrderEntityToDomain);
    }

    /**
     * Retrieves the list of orders associated with a specific user, identified by their UUID.
     *
     * @param userId the UUID of the user for whom to retrieve orders
     * @return a list of orders associated with the specified user
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(UUID userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(orderMapper::fromOrderEntityToDomain)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all orders in the system, regardless of the associated user.
     *
     * @return a list of all orders in the system
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::fromOrderEntityToDomain)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific order by its unique identifier.
     *
     * @param orderId the unique identifier of the order to retrieve
     * @return the retrieved order
     * @throws OrderNotFoundException if no order exists with the given ID
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderMapper.fromOrderEntityToDomain(orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId)));
    }

    /**
     * Modifies the status of an existing order, allowing for updates such as marking an order as completed,
     * canceled, or in progress.
     *
     * @param orderId     the unique identifier of the order to modify
     * @param orderStatus the new status to set for the order
     * @return the updated order with the modified status
     */
    @Transactional
    public Order modifyOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = getOrderById(orderId);

        if (orderStatus == OrderStatus.PAYMENT_PENDING) {
            throw new IllegalArgumentException("Use checkout endpoint to move an order to PAYMENT_PENDING");
        }

        if (!order.isActive()) {
            throw new OrderDisabledException("Order has been disabled");
        }

        List<OrderProduct> productsToRestock = order.modifyOrderStatus(orderStatus);
        
        if (productsToRestock != null && !productsToRestock.isEmpty()) {
            productService.restockProducts(productsToRestock);
        }
        
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        orderMapper.updateEntityFromDomain(order, entity, referenceMapper);
        OrderEntity updatedEntity = orderRepository.save(entity);
        return orderMapper.fromOrderEntityToDomain(updatedEntity);
    }

    /**
     * Finalizes checkout for a pending order and freezes further item mutation.
     *
     * @param orderId the unique identifier of the order to checkout
     * @return the order after moving to PAYMENT_PENDING
     */
    @Transactional
    public Order checkoutOrder(Long orderId) {
        Order order = getOrderById(orderId);

        if (!order.isActive()) {
            throw new OrderDisabledException("Order has been disabled");
        }

        if (order.getOrderProducts() == null || order.getOrderProducts().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout an order without items");
        }

        if (order.getTotalPrice() == null || order.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cannot checkout an order with a non-positive total");
        }

        UUID correlationId = order.getCheckoutCorrelationId();
        if (correlationId == null) {
            correlationId = checkoutCorrelationId(order);
        }

        order.checkout(correlationId);

        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        orderMapper.updateEntityFromDomain(order, entity, referenceMapper);
        OrderEntity updatedEntity = orderRepository.save(entity);
        Order checkedOutOrder = orderMapper.fromOrderEntityToDomain(updatedEntity);
        applicationEventPublisher.publishEvent(PaymentRequestReadyEvent.from(checkedOutOrder));
        return checkedOutOrder;
    }

    private UUID checkoutCorrelationId(Order order) {
        String source = "checkout:%d:%s".formatted(order.getId(), order.getUserId());
        return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Marks an order as inactive, effectively deleting it from active listings
     * without removing the record from the database.
     * <p>
     * Uses the domain model's delete() method to properly handle product restocking.
     *
     * @param orderId the unique identifier of the order to delete
     * @throws OrderNotFoundException if no order exists with the given ID
     *
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = getOrderById(orderId);

        if (!order.isActive()) {
            throw new OrderDisabledException("Order has been already disabled");
        }

        List<OrderProduct> productsToRestock = order.delete();

        if (productsToRestock != null && !productsToRestock.isEmpty()) {
            productService.restockProducts(productsToRestock);
        }

        // Update the JPA entity from the domain model
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        orderMapper.updateEntityFromDomain(order, entity, referenceMapper);
        orderRepository.save(entity);
    }

}
