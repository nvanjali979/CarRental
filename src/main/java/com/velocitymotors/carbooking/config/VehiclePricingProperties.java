package com.velocitymotors.carbooking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "car-booking.vehicle-pricing")
public record VehiclePricingProperties(BigDecimal compact, BigDecimal sedan, BigDecimal suv, BigDecimal luxury) {
}
