package org.java_avanzado.taller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Commerce Application Spring Boot application.
 *
 * <p>This application is a spring boot based e-commerce system that manages
 * users, products, and orders. It integrates with RabbitMQ for asynchronous
 * event-driven payment processing.</p>
 *
 * @author Java Advanced Workshop
 * @version 1.0
 */
@SpringBootApplication
public class Taller1JavaApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Taller1JavaApplication.class, args);
    }

}
