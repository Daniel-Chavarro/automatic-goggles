package org.java_avanzado.taller.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.java_avanzado.taller.support.AuditTestUtils.withAudit;

import java.math.BigDecimal;
import org.java_avanzado.taller.domain.model.OrderStatus;
import org.java_avanzado.taller.domain.model.UserRole;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Nested
    class GivenOrders {

        @Nested
        class WhenFindingAllByUserId {

            @Test
            void thenReturnsOnlyRequestedUserOrders() {
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

            @Test
            void thenReturnsEmptyListWhenUserHasNoOrders() {
                UserEntity userWithoutOrders = userRepository.save(buildUser("without.orders@example.com"));
                UserEntity otherUser = userRepository.save(buildUser("other@example.com"));
                orderRepository.save(buildOrder(otherUser, new BigDecimal("300.00")));

                var result = orderRepository.findAllByUserId(userWithoutOrders.getId());

                assertThat(result).isEmpty();
            }
        }
    }

    private UserEntity buildUser(String email) {
        return withAudit(UserEntity.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .phone("3000000000")
                .password("encoded-password")
                .role(UserRole.CLIENT)
                .active(true)
                .build());
    }

    private OrderEntity buildOrder(UserEntity user, BigDecimal totalPrice) {
        return withAudit(OrderEntity.builder()
                .user(user)
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.PENDING)
                .build());
    }
}
