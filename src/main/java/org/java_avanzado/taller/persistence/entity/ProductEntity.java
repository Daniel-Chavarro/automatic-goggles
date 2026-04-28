package org.java_avanzado.taller.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity that represents a product in the catalog.
 *
 * <p>Includes commercial data, availability, and version control for
 * optimistic concurrency handling.</p>
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEntity extends AuditableEntity {
    /**
     * Technical identifier of the product.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false, unique = true)
    private Long id;

    /**
     * Display name of the product.
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Optional description for commercial detail.
     */
    @Column(name = "description")
    private String description;

    /**
     * Current catalog price of the product.
     */
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    /**
     * Available stock for new orders.
     */
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    /**
     * Logical flag to enable or disable the product.
     */
    @Column(name = "active", nullable = false)
    private boolean active;

    /**
     * Version used for optimistic concurrency control.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
