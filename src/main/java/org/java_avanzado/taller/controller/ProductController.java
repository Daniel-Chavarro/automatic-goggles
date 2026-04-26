package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateProductRequest;
import org.java_avanzado.taller.controller.dto.response.ProductResponse;
import org.java_avanzado.taller.controller.dto.response.ProductSummaryResponse;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.utils.mapper.ProductMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes product management endpoints.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    /**
     * Returns all active products as summary data.
     *
     * @return a list of active product summaries
     */
    @GetMapping
    public ResponseEntity<List<ProductSummaryResponse>> getAllProducts() {
        return ResponseEntity.ok(productMapper.fromProductListToSummaryList(productService.getAllActiveProducts()));
    }

    /**
     * Returns a single active product by identifier.
     *
     * @param id product identifier
     * @return the product details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productMapper.fromProductToResponse(productService.getActiveProduct(id)));
    }

    /**
     * Creates a product.
     *
     * @param request validated product creation payload
     * @return the created product
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = productMapper.fromCreateProductRequestToDomain(request);
        return ResponseEntity.ok(productMapper.fromProductToResponse(productService.createProduct(product)));
    }

    /**
     * Updates an existing product.
     *
     * @param id      product identifier
     * @param request validated update payload
     * @return the updated product
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateProductRequest request) {
        Product product = productMapper.fromUpdateProductRequestToDomain(request);
        return ResponseEntity.ok(productMapper.fromProductToResponse(productService.updateProduct(id, product)));
    }

    /**
     * Deletes a product by identifier.
     *
     * @param id product identifier
     * @return an empty response with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}