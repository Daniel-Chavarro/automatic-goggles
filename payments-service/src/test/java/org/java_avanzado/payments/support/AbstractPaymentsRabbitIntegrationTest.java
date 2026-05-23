package org.java_avanzado.payments.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.awaitility.Awaitility;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
        "spring.rabbitmq.dynamic=true",
        "spring.rabbitmq.listener.simple.auto-startup=true"
})
@Import(PaymentsRabbitTestContainerConfiguration.class)
@EnabledIfDockerAvailable
public abstract class AbstractPaymentsRabbitIntegrationTest {

    private static final String QUEUE_MESSAGE_COUNT = "QUEUE_MESSAGE_COUNT";
    private static final Duration MESSAGE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(200);

    @Autowired
    protected RabbitTemplate rabbitTemplate;

    @Autowired
    protected AmqpAdmin amqpAdmin;

    protected void purgeQueue(String queueName) {
        amqpAdmin.purgeQueue(queueName, true);
    }

    protected long queueDepth(String queueName) {
        Properties queueProperties = amqpAdmin.getQueueProperties(queueName);
        assertThat(queueProperties).as("queue %s should be declared", queueName).isNotNull();
        Object count = queueProperties.get(QUEUE_MESSAGE_COUNT);
        assertThat(count).isInstanceOf(Number.class);
        return ((Number) count).longValue();
    }

    protected <T> T awaitMessage(String queueName, Class<T> payloadType) {
        AtomicReference<Object> payload = new AtomicReference<>();
        Awaitility.await()
                .atMost(MESSAGE_TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .until(() -> {
                    Object received = rabbitTemplate.receiveAndConvert(queueName);
                    if (received == null) {
                        return false;
                    }
                    payload.set(received);
                    return true;
                });
        assertThat(payload.get()).isInstanceOf(payloadType);
        return payloadType.cast(payload.get());
    }

    protected void awaitQueueDepth(String queueName, long expectedCount) {
        Awaitility.await()
                .atMost(MESSAGE_TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .untilAsserted(() -> assertThat(queueDepth(queueName)).isEqualTo(expectedCount));
    }
}
