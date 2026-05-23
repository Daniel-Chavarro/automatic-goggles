package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest;
import org.java_avanzado.taller.controller.dto.request.filter.ProductFilterDto;
import org.java_avanzado.taller.controller.dto.request.update.UpdateProductRequest;
import org.java_avanzado.taller.exception.ProductNotFoundException;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.persistence.specification.ProductSpecifications;
import org.java_avanzado.taller.utils.mapper.ProductMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Retrieves a paginated list of products based on the provided filter.
     *
     * @param filter   filter criteria
     * @param pageable pagination and sorting information
     * @return a page of products matching the filter
     */
    @Transactional(readOnly = true)
    public Page<Product> getProducts(ProductFilterDto filter, Pageable pageable) {
        Specification<ProductEntity> spec = ProductSpecifications.withFilter(filter);
        Page<ProductEntity> entityPage = productRepository.findAll(spec, pageable);
        return entityPage.map(productMapper::fromProductEntityToDomain);
    }

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
     * @param id      identifier of the product to update
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
     * Restocks products based on the provided order products list.
     * For each order product, increases the product stock by the quantity specified.
     *
     * @param orderProducts list of order products containing productId and quantity to restore
     */
    @Transactional
    @Async("taskExecutor")
    public void restockProducts(List<OrderProduct> orderProducts) {
        Map<Long, Integer> restockQuantities = new HashMap<>();
        
        for (OrderProduct orderProduct : orderProducts) {
            restockQuantities.merge(orderProduct.getProductId(), orderProduct.getQuantity(), Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry : restockQuantities.entrySet()) {
            ProductEntity entity;
            Long productId = entry.getKey();
            int quantityToRestock = entry.getValue();

            try {
                entity = productRepository.findById(productId)
                        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
            } catch (ProductNotFoundException e) {

                log.error("Failed to restock product with id {}: {}", productId, e.getMessage());
                continue;
            }


            entity.setStockQuantity(entity.getStockQuantity() + quantityToRestock);
            productRepository.save(entity);
        }
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

    public Map<Long, String> findNamesByIds(Set<Long> ids) {
        List<ProductEntity> products = productRepository.findAllById(ids);
        return products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, ProductEntity::getName));
    }
}
