package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateProductRequest;
import org.java_avanzado.taller.controller.dto.response.ProductResponse;
import org.java_avanzado.taller.controller.dto.response.ProductSummaryResponse;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.utils.mapper.ProductResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductResponseMapper productResponseMapper;

    @GetMapping
    public ResponseEntity<List<ProductSummaryResponse>> getAllProducts() {
        return ResponseEntity.ok(productResponseMapper.toSummaryList(productService.getAllActiveProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productResponseMapper.toResponse(productService.getActiveProduct(id)));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productResponseMapper.toResponse(productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productResponseMapper.toResponse(productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}