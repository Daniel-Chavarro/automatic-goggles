package org.java_avanzado.taller.config;

import org.java_avanzado.events.EventTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Configuration
public class RabbitMqConfig {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqConfig.class);
    private static final String EVENTS_PACKAGE = "org.java_avanzado.events";

    @Bean(name = "commerce.events")
    TopicExchange commerceEventsExchange() {
        return new TopicExchange(EventTopology.EXCHANGE, true, false);
    }

    @Bean(name = "commerce.payment-results")
    Queue commercePaymentResultsQueue() {
        return QueueBuilder.durable(EventTopology.COMMERCE_PAYMENT_RESULTS_QUEUE)
                .deadLetterExchange(EventTopology.EXCHANGE)
                .deadLetterRoutingKey(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ)
                .build();
    }

    @Bean(name = "commerce.payment-results.dlq")
    Queue commercePaymentResultsDlq() {
        return QueueBuilder.durable(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ).build();
    }

    @Bean
    Binding paymentSucceededBinding() {
        return BindingBuilder.bind(commercePaymentResultsQueue())
                .to(commerceEventsExchange())
                .with(EventTopology.PAYMENT_SUCCEEDED_V1);
    }

    @Bean
    Binding paymentFailedBinding() {
        return BindingBuilder.bind(commercePaymentResultsQueue())
                .to(commerceEventsExchange())
                .with(EventTopology.PAYMENT_FAILED_V1);
    }

    @Bean
    Binding commercePaymentResultsDlqBinding() {
        return BindingBuilder.bind(commercePaymentResultsDlq())
                .to(commerceEventsExchange())
                .with(EventTopology.COMMERCE_PAYMENT_RESULTS_DLQ);
    }

    @Bean
    DefaultJacksonJavaTypeMapper rabbitJacksonJavaTypeMapper() {
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setTrustedPackages(EVENTS_PACKAGE);
        return typeMapper;
    }

    @Bean
    JacksonJsonMessageConverter rabbitMessageConverter(DefaultJacksonJavaTypeMapper rabbitJacksonJavaTypeMapper) {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(EVENTS_PACKAGE);
        converter.setJavaTypeMapper(rabbitJacksonJavaTypeMapper);
        return converter;
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                  JacksonJsonMessageConverter rabbitMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(EventTopology.EXCHANGE);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                logger.error("RabbitMQ publisher confirm failed for correlation {}: {}", correlationData, cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> logger.error(
                "RabbitMQ returned unroutable message from exchange {} with routing key {}: {} {}",
                returned.getExchange(),
                returned.getRoutingKey(),
                returned.getReplyCode(),
                returned.getReplyText()));
        return rabbitTemplate;
    }

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
