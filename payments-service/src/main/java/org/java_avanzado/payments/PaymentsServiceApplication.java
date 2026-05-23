package org.java_avanzado.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main entry point for the Payments Service Spring Boot application.
 *
 * <p>This service processes payment requests from the commerce system via RabbitMQ.
 * It simulates payment processing and publishes payment results back to the commerce
 * service for order fulfillment.</p>
 *
 * @author Java Advanced Workshop
 * @version 1.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PaymentsServiceApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentsServiceApplication.class, args);
    }
}
