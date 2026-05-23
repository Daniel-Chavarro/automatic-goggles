package org.java_avanzado.taller.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class CommerceRabbitTestContainerConfiguration {

    private static final DockerImageName RABBITMQ_IMAGE = DockerImageName.parse("rabbitmq:4.1-management");

    @Bean
    @ServiceConnection
    RabbitMQContainer rabbitMqContainer() {
        return new RabbitMQContainer(RABBITMQ_IMAGE);
    }
}
