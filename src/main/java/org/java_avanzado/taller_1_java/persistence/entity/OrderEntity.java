package org.java_avanzado.taller_1_java.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.java_avanzado.taller_1_java.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Entity that represents a purchase order.
 *
 * <p>It stores the order total, its current status, and the related item
 * collection in {@link OrderProductEntity}.</p>
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity extends AuditableEntity {
    /** Technical identifier of the order. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false, unique = true)
    private Long id;

    /** Total amount of the order at persistence time. */
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    /** Business status of the order in the workflow. */
    @Column(name="order_status", nullable = false)
    private OrderStatus orderStatus;

    /** Version value used for optimistic concurrency control. */
    @Column(name = "version", nullable = false)
    private BigInteger version;

    /** Order items with full cascade and orphan removal. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProductEntity> orderProducts;

    /** Owner user of the order. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity users;
}
