package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.exception.ProductNotFoundException;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.utils.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Product getActiveProduct(Long id) {
        ProductEntity entity = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found or disabled"));
        return productMapper.toDomain(entity);
    }

    @Transactional
    public Product createProduct(Product product) {
        ProductEntity entity = productMapper.toEntity(product);
        entity.setActive(true);
        return productMapper.toDomain(productRepository.save(entity));
    }

    @Transactional
    public Product updateProduct(Long id, Product product) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPrice(product.getPrice());
        entity.setStockQuantity(product.getQuantity());
        return productMapper.toDomain(productRepository.save(entity));
    }

    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        entity.setActive(false);
        productRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public java.util.List<Product> getAllActiveProducts() {
        return productRepository.findAll().stream()
                .filter(ProductEntity::isActive)
                .map(productMapper::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }
}
