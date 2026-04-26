package org.java_avanzado.taller.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest;
import org.java_avanzado.taller.controller.dto.request.update.UpdateProductRequest;
import org.java_avanzado.taller.controller.dto.response.ProductResponse;
import org.java_avanzado.taller.controller.dto.request.filter.ProductFilterDto;
import org.java_avanzado.taller.controller.dto.response.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import org.java_avanzado.taller.controller.dto.response.ProductSummaryResponse;

import java.util.List;

@Tag(name = "Products", description = "Product catalog management operations")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Operation(summary = "List all products", description = "Returns a paginated list of active products with optional filtering by name, price range, and active status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductSummaryResponse>> getAllProducts(
            @Parameter(description = "Filter criteria for products") ProductFilterDto filter,
            @Parameter(description = "Pagination and sorting information") Pageable pageable) {
        Page<Product> products = productService.getProducts(filter, pageable);
        Page<ProductSummaryResponse> responsePage = products.map(productMapper::fromProductToSummary);
        return ResponseEntity.ok(PaginatedResponse.from(responsePage));
    }

    @Operation(summary = "Get product by ID", description = "Returns a single active product by its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Unique identifier of the product", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(productMapper.fromProductToResponse(productService.getActiveProduct(id)));
    }

    @Operation(summary = "Create a new product", description = "Creates a new product with the provided information including name, description, price, and initial stock quantity.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product creation payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateProductRequest.class)))
            @Valid @RequestBody CreateProductRequest request) {
        Product product = productMapper.fromCreateProductRequestToDomain(request);
        return ResponseEntity.ok(productMapper.fromProductToResponse(productService.createProduct(product)));
    }

    @Operation(summary = "Update a product", description = "Partially updates an existing product. Only provided fields will be updated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Unique identifier of the product", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Fields to update",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateProductRequest.class)))
            @Valid @RequestBody UpdateProductRequest request) {
        Product product = productMapper.fromUpdateProductRequestToDomain(request);
        return ResponseEntity.ok(productMapper.fromProductToResponse(productService.updateProduct(id, product)));
    }

    @Operation(summary = "Delete a product", description = "Soft-deletes a product by marking it as inactive. The product will no longer appear in active product listings.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Unique identifier of the product", required = true) @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}