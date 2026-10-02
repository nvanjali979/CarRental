package com.velocitymotors.carbooking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "car-booking.credit-card-validation")
public record CarBookingProperties(
        String BaseURL, String Path) {
}
