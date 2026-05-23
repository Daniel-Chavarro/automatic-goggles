package org.java_avanzado.payments.service;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for payment simulation service.
 *
 * <p>These properties control the behavior of the payment simulation,
 * particularly the threshold amount above which payments fail.</p>
 *
 * @param maxSuccessAmount the maximum amount for a payment to succeed (default: 1000)
 */
@ConfigurationProperties(prefix = "app.payments")
public record PaymentSimulationProperties(BigDecimal maxSuccessAmount) {

    public PaymentSimulationProperties {
        if (maxSuccessAmount == null) {
            maxSuccessAmount = BigDecimal.valueOf(1000);
        }
    }
}
