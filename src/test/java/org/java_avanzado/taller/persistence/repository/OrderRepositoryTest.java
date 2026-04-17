package org.java_avanzado.taller.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import org.java_avanzado.taller.domain.model.OrderStatus;
import org.java_avanzado.taller.domain.model.UserRole;
import org.java_avanzado.taller.persistence.entity.AuditableEntity;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void givenOrdersForDifferentUsers_whenFindAllByUserId_thenReturnsOnlyRequestedUserOrders() {
        UserEntity targetUser = userRepository.save(buildUser("target@example.com"));
        UserEntity otherUser = userRepository.save(buildUser("other@example.com"));

        OrderEntity firstTargetOrder = orderRepository.save(buildOrder(targetUser, new BigDecimal("100.00")));
        OrderEntity secondTargetOrder = orderRepository.save(buildOrder(targetUser, new BigDecimal("150.00")));
        orderRepository.save(buildOrder(otherUser, new BigDecimal("300.00")));

        var result = orderRepository.findAllByUserId(targetUser.getId());

        assertThat(result)
                .extracting(OrderEntity::getId)
                .containsExactlyInAnyOrder(firstTargetOrder.getId(), secondTargetOrder.getId());
        assertThat(result).allMatch(order -> order.getUser().getId().equals(targetUser.getId()));
    }

    private UserEntity buildUser(String email) {
        UserEntity entity = UserEntity.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .phone("3000000000")
                .password("encoded-password")
                .role(UserRole.CLIENT)
                .active(true)
                .build();
        stampAudit(entity);
        return entity;
    }

    private OrderEntity buildOrder(UserEntity user, BigDecimal totalPrice) {
        OrderEntity entity = OrderEntity.builder()
                .user(user)
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.PENDING)
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
