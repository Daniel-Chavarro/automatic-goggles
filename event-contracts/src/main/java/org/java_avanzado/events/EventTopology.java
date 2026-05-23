package org.java_avanzado.events;

/**
 * Centralized configuration for RabbitMQ event topology.
 *
 * <p>This class defines all the exchange names, queue names, and routing keys
 * used for event-driven communication between the commerce and payment services.
 * All constants are public and static for easy access throughout the application.</p>
 *
 * @author Java Advanced Workshop
 * @version 1.0
 */
public final class EventTopology {

    /**
     * The main RabbitMQ exchange name for all commerce events.
     */
    public static final String EXCHANGE = "commerce.events";

    // Event routing keys
    /**
     * Routing key for order payment requested events.
     */
    public static final String ORDER_PAYMENT_REQUESTED_V1 = "order.payment.requested.v1";
    /**
     * Routing key for payment succeeded events.
     */
    public static final String PAYMENT_SUCCEEDED_V1 = "payment.succeeded.v1";
    /**
     * Routing key for payment failed events.
     */
    public static final String PAYMENT_FAILED_V1 = "payment.failed.v1";

    // Commerce app queues for payment results
    /**
     * Queue name for payment results in the commerce service.
     */
    public static final String COMMERCE_PAYMENT_RESULTS_QUEUE = "commerce.payment-results";
    /**
     * Dead letter queue for failed payment results in the commerce service.
     */
    public static final String COMMERCE_PAYMENT_RESULTS_DLQ = "commerce.payment-results.dlq";

    // Payments service queues for payment requests
    /**
     * Queue name for payment requests in the payments service.
     */
    public static final String PAYMENTS_PAYMENT_REQUESTS_QUEUE = "payments.payment-requests";
    /**
     * Dead letter queue for failed payment requests in the payments service.
     */
    public static final String PAYMENTS_PAYMENT_REQUESTS_DLQ = "payments.payment-requests.dlq";

    private EventTopology() {
    }
}
