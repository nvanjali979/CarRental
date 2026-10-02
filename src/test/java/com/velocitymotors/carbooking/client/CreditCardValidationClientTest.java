package com.velocitymotors.carbooking.client;

import com.velocitymotors.carbooking.config.CarBookingProperties;
import com.velocitymotors.carbooking.exception.CreditCardValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CreditCardValidationClientTest {

    private static final String BASE_URL = "http://localhost:9090/host/credit-card-payment-api";

    private MockRestServiceServer mockServer;
    private CreditCardValidationClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        CarBookingProperties properties = new CarBookingProperties(BASE_URL, "/payment-status");
        client = new CreditCardValidationClient(restClient, properties);
    }

    @Test
    void validate_approvedResponse_returnsApprovedStatus() {
        mockServer.expect(requestTo(BASE_URL + "/payment-status"))
                .andRespond(withSuccess("{\"lastUpdateDate\":\"2026-01-01\",\"status\":\"APPROVED\"}", MediaType.APPLICATION_JSON));

        PaymentStatusResponse response = client.validate(new PaymentStatusRetrievalRequest("PAY123"));

        assertThat(response.status()).isEqualTo("APPROVED");
    }

    @Test
    void validate_declinedResponse_returnsDeclinedStatus() {
        mockServer.expect(requestTo(BASE_URL + "/payment-status"))
                .andRespond(withSuccess("{\"lastUpdateDate\":\"2026-01-01\",\"status\":\"DECLINED\"}", MediaType.APPLICATION_JSON));

        PaymentStatusResponse response = client.validate(new PaymentStatusRetrievalRequest("PAY123"));

        assertThat(response.status()).isEqualTo("DECLINED");
    }

    @Test
    void validate_serviceReturns404_throwsCreditCardValidationExceptionIndicatingServiceDown() {
        mockServer.expect(requestTo(BASE_URL + "/payment-status"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.validate(new PaymentStatusRetrievalRequest("PAY123")))
                .isInstanceOf(CreditCardValidationException.class)
                .hasMessageContaining("down");
    }

    @Test
    void validate_serviceReturns400_throwsCreditCardValidationExceptionForInvalidRequest() {
        mockServer.expect(requestTo(BASE_URL + "/payment-status"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> client.validate(new PaymentStatusRetrievalRequest("PAY123")))
                .isInstanceOf(CreditCardValidationException.class)
                .hasMessageContaining("Invalid Request");
    }

    @Test
    void validate_emptyBody_throwsCreditCardValidationException() {
        mockServer.expect(requestTo(BASE_URL + "/payment-status"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.validate(new PaymentStatusRetrievalRequest("PAY123")))
                .isInstanceOf(CreditCardValidationException.class);
    }
}
