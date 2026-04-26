package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateProductRequest;
import org.java_avanzado.taller.domain.exception.ProductNotFoundException;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.utils.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.java_avanzado.taller.utils.validators.AuxiliaryMethods.modify;

/**
 * Service for product catalog management operations.
 *
 * <p>Handles product creation, retrieval, updates, and deletion
 * within the coffee shop product catalog.</p>
 */
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Retrieves an active product by its identifier.
     *
     * @param id the identifier of the product to retrieve
     * @return the active product with the specified identifier
     *
     */
    @Transactional(readOnly = true)
    public Product getActiveProduct(Long id) {
        ProductEntity entity = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found or disabled"));
        return productMapper.fromProductEntityToDomain(entity);
    }

    /**
     * Creates a product after validating required business rules.
     *
     * @param product product data to persist
     * @return the persisted product
     * @throws IllegalArgumentException when the product data is invalid or the name already exists
     */
    @Transactional
    public Product createProduct(Product product) {
        validateProduct(product);

        String name = product.getName().strip().toUpperCase();

        if (productRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Product name already exists");
        }

        ProductEntity entity = productMapper.fromProductToEntity(product);

        return productMapper.fromProductEntityToDomain(productRepository.save(entity));
    }

    /**
     * Updates an existing product with the provided values.
     *
     * @param id identifier of the product to update
     * @param product product data used to update the target entity
     * @return the updated product
     * @throws ProductNotFoundException when no product exists with the provided identifier
     * @throws IllegalArgumentException when the provided name already exists
     */
    @Transactional
    public Product updateProduct(Long id, Product product) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        String name = product.getName().strip().toUpperCase();

        if (productRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Product name already exists");
        }

        modify(name, entity::setName);
        modify(product.getDescription(), entity::setDescription);
        modify(product.getPrice(), entity::setPrice);
        modify(product.getQuantity(), entity::setStockQuantity);

        return productMapper.fromProductEntityToDomain(productRepository.save(entity));
    }

    /**
     * Performs a logical deletion by marking a product as inactive.
     *
     * @param id identifier of the product to disable
     * @throws ProductNotFoundException when no product exists with the provided identifier
     */
    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        entity.setActive(false);
        productRepository.save(entity);
    }

    /**
     * Returns all active products.
     *
     * @return list of active products
     * @deprecated use repository/domain-based active queries instead of loading all products in memory
     */
    @Transactional(readOnly = true)
    @Deprecated
    public List<Product> getAllActiveProducts() {
        return productRepository.findAll().stream()
                .filter(ProductEntity::isActive)
                .map(productMapper::fromProductEntityToDomain)
                .collect(Collectors.toList());
    }

    /**
     * Legacy overload that creates a product directly from request DTO data.
     *
     * @param request request payload used to build the product
     * @return the persisted product
     * @deprecated use {@link #createProduct(Product)} after mapping in the controller layer
     */
    @Transactional
    @Deprecated(forRemoval = true)
    public Product createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .active(true)
                .build();

        ProductEntity entity = productMapper.fromProductToEntity(product);
        return productMapper.fromProductEntityToDomain(productRepository.save(entity));
    }

    /**
     * Legacy overload that updates a product directly from request DTO data.
     *
     * @param id identifier of the product to update
     * @param request request payload with partial update values
     * @return the updated product
     * @throws ProductNotFoundException when no product exists with the provided identifier
     * @deprecated use {@link #updateProduct(Long, Product)} after mapping in the controller layer
     */
    @Transactional
    @Deprecated(forRemoval = true)
    public Product updateProduct(Long id, UpdateProductRequest request) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            entity.setPrice(request.getPrice());
        }
        if (request.getQuantity() != null) {
            entity.setStockQuantity(request.getQuantity());
        }

        return productMapper.fromProductEntityToDomain(productRepository.save(entity));
    }

    /**
     * Validates basic product constraints before persistence operations.
     *
     * @param product product to validate
     * @throws IllegalArgumentException when name is missing, price is not positive, or quantity is negative
     */
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }

        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }

    }
}
