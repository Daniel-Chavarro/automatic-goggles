package org.java_avanzado.taller.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import org.java_avanzado.taller.persistence.entity.AuditableEntity;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void givenMixedProducts_whenFindByIdAndActiveTrue_thenReturnsOnlyActiveProductById() {
        ProductEntity activeProduct = productRepository.save(buildProduct("Laptop", true, new BigDecimal("2500.00")));
        ProductEntity inactiveProduct = productRepository.save(buildProduct("Laptop", false, new BigDecimal("2100.00")));

        var activeResult = productRepository.findByIdAndActiveTrue(activeProduct.getId());
        var inactiveResult = productRepository.findByIdAndActiveTrue(inactiveProduct.getId());

        assertThat(activeResult).isPresent();
        assertThat(activeResult.get().isActive()).isTrue();
        assertThat(activeResult.get().getId()).isEqualTo(activeProduct.getId());
        assertThat(inactiveResult).isEmpty();
    }

    @Test
    void givenProductsWithSameAndDifferentName_whenFindAllByName_thenReturnsOnlyMatchingName() {
        ProductEntity firstCoffee = productRepository.save(buildProduct("Coffee", true, new BigDecimal("10.00")));
        ProductEntity secondCoffee = productRepository.save(buildProduct("Coffee", false, new BigDecimal("12.00")));
        productRepository.save(buildProduct("Tea", true, new BigDecimal("8.00")));

        var results = productRepository.findAllByName("Coffee");

        assertThat(results)
                .extracting(ProductEntity::getId)
                .containsExactlyInAnyOrder(firstCoffee.getId(), secondCoffee.getId());
        assertThat(results).allMatch(product -> "Coffee".equals(product.getName()));
    }

    private ProductEntity buildProduct(String name, boolean active, BigDecimal price) {
        ProductEntity entity = ProductEntity.builder()
                .name(name)
                .description(name + " description")
                .price(price)
                .stockQuantity(10)
                .active(active)
                .build();
        stampAudit(entity);
        return entity;
    }

    private void stampAudit(AuditableEntity entity) {
        ReflectionTestUtils.setField(entity, "createdAt", Instant.now());
        ReflectionTestUtils.setField(entity, "createdBy", "repository-test");
        ReflectionTestUtils.setField(entity, "updatedAt", Instant.now());
        ReflectionTestUtils.setField(entity, "updatedBy", "repository-test");
    }
}
