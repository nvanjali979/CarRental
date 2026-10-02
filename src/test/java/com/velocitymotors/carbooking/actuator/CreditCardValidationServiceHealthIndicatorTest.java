package com.velocitymotors.carbooking.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CreditCardValidationServiceHealthIndicatorTest {

    private static final String BASE_URL = "http://localhost:9090/host/credit-card-payment-api";

    private MockRestServiceServer mockServer;
    private CreditCardValidationServiceHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        healthIndicator = new CreditCardValidationServiceHealthIndicator(builder.build());
    }

    @Test
    void health_serviceRespondsSuccessfully_isUp() {
        mockServer.expect(requestTo(BASE_URL + "/"))
                .andExpect(method(org.springframework.http.HttpMethod.HEAD))
                .andRespond(withSuccess());

        Health health = healthIndicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("UP");
    }

    @Test
    void health_serviceRespondsWithErrorStatus_isStillUpBecauseServiceIsReachable() {
        mockServer.expect(requestTo(BASE_URL + "/"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        Health health = healthIndicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("UP");
    }

    @Test
    void health_connectionFails_isDown() {
        mockServer.expect(requestTo(BASE_URL + "/"))
                .andRespond(request -> {
                    throw new IOException("Connection refused");
                });

        Health health = healthIndicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("DOWN");
    }
}
