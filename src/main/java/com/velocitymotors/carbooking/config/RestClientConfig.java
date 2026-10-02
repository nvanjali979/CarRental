package com.velocitymotors.carbooking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CarBookingProperties.class)
public class RestClientConfig {

    private final CarBookingProperties carBookingProperties;

    @Autowired
    public RestClientConfig(CarBookingProperties carBookingProperties) {
        this.carBookingProperties = carBookingProperties;
    }

    @Bean
    public RestClient crediCardValidationRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3_000);
        requestFactory.setReadTimeout(5_000);

        return RestClient.builder().baseUrl(carBookingProperties.BaseURL()).requestFactory(requestFactory).build();
    }
}
