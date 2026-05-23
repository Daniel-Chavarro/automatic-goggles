package org.java_avanzado.taller.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_avanzado.events.PaymentFailedEvent;
import org.java_avanzado.events.PaymentSucceededEvent;
import org.java_avanzado.taller.domain.model.Order;
import org.java_avanzado.taller.domain.model.OrderProduct;
import org.java_avanzado.taller.domain.model.enums.OrderStatus;
import org.java_avanzado.taller.persistence.entity.OrderEntity;
import org.java_avanzado.taller.persistence.entity.ProcessedPaymentResultEntity;
import org.java_avanzado.taller.persistence.repository.OrderRepository;
import org.java_avanzado.taller.persistence.repository.ProcessedPaymentResultRepository;
import org.java_avanzado.taller.utils.mapper.OrderMapper;
import org.java_avanzado.taller.utils.mapper.ReferenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for processing payment result events from the payment service.
 *
 * <p>Handles both successful and failed payment events, updating order statuses,
 * restoring product stock when needed, and ensuring idempotency for payment result
 * processing. Uses transactions to maintain data consistency.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentResultService {

    private static final int SUPPORTED_SCHEMA_VERSION = 1;
    private static final String SUCCEEDED = "SUCCEEDED";
    private static final String FAILED = "FAILED";

    private final ProcessedPaymentResultRepository processedPaymentResultRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ReferenceMapper referenceMapper;
    private final ProductService productService;
    private final ProcessedPaymentResultIdempotencyService processedPaymentResultIdempotencyService;

    @Transactional
    public void apply(PaymentSucceededEvent event) {
        validateSucceeded(event);
        orderRepository.findByIdForPaymentResultUpdate(event.orderId()).ifPresentOrElse(entity -> {
            if (markProcessedIfNew(event.eventId(), event.orderId(), SUCCEEDED)) {
                applySuccess(event, entity);
            }
        }, () -> markUnknownOrderProcessed(event.eventId(), event.orderId(), SUCCEEDED));
    }

    @Transactional
    public void apply(PaymentFailedEvent event) {
        validateFailed(event);
        orderRepository.findByIdForPaymentResultUpdate(event.orderId()).ifPresentOrElse(entity -> {
            if (markProcessedIfNew(event.eventId(), event.orderId(), FAILED)) {
                applyFailure(event, entity);
            }
        }, () -> markUnknownOrderProcessed(event.eventId(), event.orderId(), FAILED));
    }

    private boolean markProcessedIfNew(UUID eventId, Long orderId, String resultType) {
        if (processedPaymentResultRepository.existsById(eventId)) {
            log.info("Ignoring duplicate payment result event {}", eventId);
            return false;
        }

        processedPaymentResultRepository.saveAndFlush(ProcessedPaymentResultEntity.builder()
                .eventId(eventId)
                .orderId(orderId)
                .resultType(resultType)
                .build());
        return true;
    }

    private void markUnknownOrderProcessed(UUID eventId, Long orderId, String resultType) {
        if (processedPaymentResultIdempotencyService.markUnknownOrderProcessedIfNew(eventId, orderId, resultType)) {
            log.warn("Ignoring payment {} event {} for unknown order {}", resultType.toLowerCase(), eventId, orderId);
        } else {
            log.info("Ignoring duplicate payment result event {}", eventId);
        }
    }

    private void applySuccess(PaymentSucceededEvent event, OrderEntity entity) {
        Order order = orderMapper.fromOrderEntityToDomain(entity);
        if (!order.approvePayment()) {
            logIgnoredTerminalResult(event.eventId(), event.orderId(), order.getOrderStatus(), SUCCEEDED);
            return;
        }

        orderMapper.updateEntityFromDomain(order, entity, referenceMapper);
        orderRepository.save(entity);
    }

    private void applyFailure(PaymentFailedEvent event, OrderEntity entity) {
        Order order = orderMapper.fromOrderEntityToDomain(entity);
        List<OrderProduct> productsToRestock = order.rejectPayment();
        if (productsToRestock == null) {
            logIgnoredTerminalResult(event.eventId(), event.orderId(), order.getOrderStatus(), FAILED);
            return;
        }

        orderMapper.updateEntityFromDomain(order, entity, referenceMapper);
        orderRepository.save(entity);
        if (!productsToRestock.isEmpty()) {
            productService.restockProducts(productsToRestock);
        }
    }

    private void logIgnoredTerminalResult(UUID eventId, Long orderId, OrderStatus currentStatus, String resultType) {
        log.info(
                "Ignoring {} payment result event {} for order {} because current status is {}",
                resultType,
                eventId,
                orderId,
                currentStatus);
    }

    private void validateSucceeded(PaymentSucceededEvent event) {
        if (event == null
                || event.eventId() == null
                || event.schemaVersion() != SUPPORTED_SCHEMA_VERSION
                || event.correlationId() == null
                || event.orderId() == null
                || event.userId() == null
                || event.amount() == null
                || event.paymentId() == null
                || event.occurredAt() == null) {
            throw new IllegalArgumentException("Malformed payment success event");
        }
    }

    private void validateFailed(PaymentFailedEvent event) {
        if (event == null
                || event.eventId() == null
                || event.schemaVersion() != SUPPORTED_SCHEMA_VERSION
                || event.correlationId() == null
                || event.orderId() == null
                || event.userId() == null
                || event.amount() == null
                || event.paymentId() == null
                || event.failureCode() == null
                || event.failureReason() == null
                || event.occurredAt() == null) {
            throw new IllegalArgumentException("Malformed payment failure event");
        }
    }
}
