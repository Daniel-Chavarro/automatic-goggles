package org.java_avanzado.taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.java_avanzado.taller.domain.exception.ProductNotFoundException;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.persistence.entity.ProductEntity;
import org.java_avanzado.taller.persistence.repository.ProductRepository;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.utils.mapper.ProductMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Nested
    class GivenGetActiveProduct {

        @Test
        void given_existingActiveProduct_when_getActiveProduct_then_returnsMappedDomain() {
            long productId = 1L;
            var productEntity = TestDataFactory.productEntity();
            var expectedProduct = TestDataFactory.product();

            when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(productEntity));
            when(productMapper.fromProductEntityToDomain(productEntity)).thenReturn(expectedProduct);

            var result = productService.getActiveProduct(productId);

            assertSame(expectedProduct, result);
            verify(productRepository).findByIdAndActiveTrue(productId);
            verify(productMapper).fromProductEntityToDomain(productEntity);
        }

        @Test
        void given_missingActiveProduct_when_getActiveProduct_then_throwsProductNotFoundException() {
            long missingId = 404L;
            when(productRepository.findByIdAndActiveTrue(missingId)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () -> productService.getActiveProduct(missingId));

            verify(productRepository).findByIdAndActiveTrue(missingId);
            verifyNoInteractions(productMapper);
        }
    }

    @Nested
    class GivenGetAllActiveProducts {

        @Test
        void given_mixedActiveAndInactiveProducts_when_getAllActiveProducts_then_returnsOnlyActiveProducts() {
            ProductEntity firstActiveEntity = TestDataFactory.productEntity();
            ProductEntity inactiveEntity = TestDataFactory.productEntity();
            ProductEntity secondActiveEntity = TestDataFactory.productEntity();
            ReflectionTestUtils.setField(inactiveEntity, "active", false);

            var firstActiveProduct = TestDataFactory.product();
            Product secondActiveProduct = org.mockito.Mockito.mock(Product.class);

            when(productRepository.findAll()).thenReturn(List.of(firstActiveEntity, inactiveEntity, secondActiveEntity));
            when(productMapper.fromProductEntityToDomain(firstActiveEntity)).thenReturn(firstActiveProduct);
            when(productMapper.fromProductEntityToDomain(secondActiveEntity)).thenReturn(secondActiveProduct);

            var result = productService.getAllActiveProducts();

            assertEquals(2, result.size());
            assertSame(firstActiveProduct, result.get(0));
            assertSame(secondActiveProduct, result.get(1));
            verify(productRepository).findAll();
            verify(productMapper).fromProductEntityToDomain(firstActiveEntity);
            verify(productMapper).fromProductEntityToDomain(secondActiveEntity);
        }
    }
}
