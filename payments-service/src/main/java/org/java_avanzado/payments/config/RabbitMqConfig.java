package org.java_avanzado.payments.config;

import org.java_avanzado.events.EventTopology;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for RabbitMQ infrastructure in the payments service.
 *
 * <p>Configures the RabbitMQ exchange, queues, bindings, and message converters
 * for event-driven communication between the commerce and payment services.
 * Includes dead letter queue routing for failed message handling.</p>
 *
 * @author Java Advanced Workshop
 * @version 1.0
 */
@Configuration
public class RabbitMqConfig {

    private static final String EVENTS_PACKAGE = "org.java_avanzado.events";

    /**
     * Creates the main commerce events TopicExchange.
     *
     * @return the commerce events exchange
     */
    @Bean(name = "commerce.events")
    TopicExchange commerceEventsExchange() {
        return new TopicExchange(EventTopology.EXCHANGE, true, false);
    }

    /**
     * Creates the queue for payment requests in the payments service.
     *
     * <p>Durable queue with dead letter exchange routing for messages that fail.</p>
     *
     * @return the payment requests queue
     */
    @Bean(name = "payments.payment-requests")
    Queue paymentsPaymentRequestsQueue() {
        return QueueBuilder.durable(EventTopology.PAYMENTS_PAYMENT_REQUESTS_QUEUE)
                .deadLetterExchange(EventTopology.EXCHANGE)
                .deadLetterRoutingKey(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ)
                .build();
    }

    /**
     * Creates the dead letter queue for payment requests.
     *
     * <p>Durable queue for handling messages that fail to process from the main queue.</p>
     *
     * @return the payment requests dead letter queue
     */
    @Bean(name = "payments.payment-requests.dlq")
    Queue paymentsPaymentRequestsDlq() {
        return QueueBuilder.durable(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ).build();
    }

    /**
     * Creates the binding for payment request events.
     *
     * <p>Binds the payment requests queue to the commerce events exchange
     * with the ORDER_PAYMENT_REQUESTED_V1 routing key.</p>
     *
     * @return the binding for payment requests
     */
    @Bean
    Binding paymentRequestedBinding() {
        return BindingBuilder.bind(paymentsPaymentRequestsQueue())
                .to(commerceEventsExchange())
                .with(EventTopology.ORDER_PAYMENT_REQUESTED_V1);
    }

    /**
     * Creates the binding for the payment requests dead letter queue.
     *
     * <p>Binds the dead letter queue to the commerce events exchange
     * for messages that fail processing.</p>
     *
     * @return the binding for the dead letter queue
     */
    @Bean
    Binding paymentsPaymentRequestsDlqBinding() {
        return BindingBuilder.bind(paymentsPaymentRequestsDlq())
                .to(commerceEventsExchange())
                .with(EventTopology.PAYMENTS_PAYMENT_REQUESTS_DLQ);
    }

    /**
     * Creates the Jackson Java type mapper for RabbitMQ message deserialization.
     *
     * <p>Configures the type mapper to trust the events package for unmarshalling.</p>
     *
     * @return the configured Jackson Java type mapper
     */
    @Bean
    DefaultJacksonJavaTypeMapper rabbitJacksonJavaTypeMapper() {
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setTrustedPackages(EVENTS_PACKAGE);
        return typeMapper;
    }

    /**
     * Creates the Jackson JSON message converter for RabbitMQ.
     *
     * <p>Configures conversion of JSON messages to/from event objects.</p>
     *
     * @param rabbitJacksonJavaTypeMapper the type mapper for object deserialization
     * @return the configured Jackson message converter
     */
    @Bean
    JacksonJsonMessageConverter rabbitMessageConverter(DefaultJacksonJavaTypeMapper rabbitJacksonJavaTypeMapper) {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(EVENTS_PACKAGE);
        converter.setJavaTypeMapper(rabbitJacksonJavaTypeMapper);
        return converter;
    }

    /**
     * Creates the RabbitTemplate for sending messages to RabbitMQ.
     *
     * <p>Configures the template with the commerce events exchange, message converter,
     * and mandatory delivery flag for reliable message publishing.</p>
     *
     * @param connectionFactory the RabbitMQ connection factory
     * @param rabbitMessageConverter the message converter for serialization
     * @return the configured RabbitTemplate
     */
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                  JacksonJsonMessageConverter rabbitMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(EventTopology.EXCHANGE);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    /**
     * Creates the SimpleRabbitListenerContainerFactory for consuming messages.
     *
     * <p>Configures the listener container with connection factory, message converter,
     * auto-startup capability, and sets default requeue rejection to false.</p>
     *
     * @param connectionFactory the RabbitMQ connection factory
     * @param rabbitMessageConverter the message converter for deserialization
     * @param listenerAutoStartup whether listeners should auto-start (configurable)
     * @return the configured listener container factory
     */
    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter rabbitMessageConverter,
            @Value("${spring.rabbitmq.listener.simple.auto-startup:true}") boolean listenerAutoStartup) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setAutoStartup(listenerAutoStartup);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
