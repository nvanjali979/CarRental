package com.velocitymotors.carbooking.actuator;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CreditCardValidationServiceHealthIndicator implements HealthIndicator {

    private final RestClient restClient;

    public CreditCardValidationServiceHealthIndicator(RestClient creditCardValidationRestClient) {
        this.restClient = creditCardValidationRestClient;
    }

    @Override
    public Health health() {
        try {
            restClient.head().uri("/").retrieve().toBodilessEntity();
            return Health.up().build();
        } catch (ResourceAccessException ex) {
            return Health.down(ex).build();
        } catch (RestClientException ex) {
            // Any HTTP response, even an error one, means the service is reachable.
            return Health.up().build();
        }
    }
}