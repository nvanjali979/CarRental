package com.velocitymotors.carbooking.client;

import com.velocitymotors.carbooking.exception.CreditCardValidationException;
import com.velocitymotors.carbooking.config.CarBookingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class CreditCardValidationClient {

    private static final Logger logger = LoggerFactory.getLogger(CreditCardValidationClient.class);
    public final RestClient restClient;
    private final CarBookingProperties carBookingProperties;

    @Autowired
    public CreditCardValidationClient(RestClient restClient, CarBookingProperties carBookingProperties) {
        this.carBookingProperties = carBookingProperties;
        this.restClient = restClient;
    }

    public PaymentStatusResponse validate(PaymentStatusRetrievalRequest request) {
        try {
            logger.info("path: " + carBookingProperties.Path());
            PaymentStatusResponse response = restClient.post()
                    .uri(carBookingProperties.Path())
                    .body(request)
                    .retrieve()
                    .body(PaymentStatusResponse.class);

            if (response == null || response.status() == null) {
                throw new CreditCardValidationException(
                        "credit-card-validation-service returned an empty response for reference " + request.paymentReference(), null);
            }
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            throw new CreditCardValidationException("Credit card Service is down", e);
        } catch (HttpClientErrorException e) {
            throw new CreditCardValidationException("Invalid Request", e);
        }

    }
}
