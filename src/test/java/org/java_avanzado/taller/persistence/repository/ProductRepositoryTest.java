package org.java_avanzado.taller.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.java_avanzado.taller.support.AuditTestUtils.withAudit;

import java.math.BigDecimal;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Nested
    class GivenProducts {

        @Nested
        class WhenFindingByIdAndActiveTrue {

            @Test
            void given_activeAndInactiveProductsWithSameName_when_findingByIdAndActiveTrue_then_returnsOnlyActiveProductById() {
                ProductEntity activeProduct = productRepository.save(buildProduct("Laptop", true, new BigDecimal("2500.00")));
                ProductEntity inactiveProduct = productRepository.save(buildProduct("Laptop", false, new BigDecimal("2100.00")));

                var activeResult = productRepository.findByIdAndActiveTrue(activeProduct.getId());
                var inactiveResult = productRepository.findByIdAndActiveTrue(inactiveProduct.getId());

                assertThat(activeResult).isPresent();
                assertThat(activeResult.get().isActive()).isTrue();
                assertThat(activeResult.get().getId()).isEqualTo(activeProduct.getId());
                assertThat(inactiveResult).isEmpty();
            }
        }

        @Nested
        class WhenFindingAllByName {

            @Test
            void given_productsWithDifferentNames_when_findingAllByName_then_returnsOnlyProductsWithMatchingName() {
                ProductEntity firstCoffee = productRepository.save(buildProduct("Coffee", true, new BigDecimal("10.00")));
                ProductEntity secondCoffee = productRepository.save(buildProduct("Coffee", false, new BigDecimal("12.00")));
                productRepository.save(buildProduct("Tea", true, new BigDecimal("8.00")));

                var results = productRepository.findAllByName("Coffee");

                assertThat(results)
                        .extracting(ProductEntity::getId)
                        .containsExactlyInAnyOrder(firstCoffee.getId(), secondCoffee.getId());
                assertThat(results).allMatch(product -> "Coffee".equals(product.getName()));
            }

            @Test
            void given_noProductsWithRequestedName_when_findingAllByName_then_returnsEmptyList() {
                productRepository.save(buildProduct("Tea", true, new BigDecimal("8.00")));

                var results = productRepository.findAllByName("Coffee");

                assertThat(results).isEmpty();
            }
        }
    }

    private ProductEntity buildProduct(String name, boolean active, BigDecimal price) {
        return withAudit(ProductEntity.builder()
                .name(name)
                .description(name + " description")
                .price(price)
                .stockQuantity(10)
                .active(active)
                .build());
    }
}
