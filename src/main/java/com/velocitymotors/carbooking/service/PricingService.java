package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.config.VehiclePricingProperties;
import com.velocitymotors.carbooking.domain.VehicleCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@EnableConfigurationProperties(VehiclePricingProperties.class)
public class PricingService {

    private final VehiclePricingProperties pricing;

    @Autowired
    public PricingService(VehiclePricingProperties pricing) {
        this.pricing = pricing;
    }

    public BigDecimal calculateAmount(VehicleCategory vehicleCategory, LocalDate startDate, LocalDate endDate) {
        Long days = ChronoUnit.DAYS.between(startDate, endDate);
        return BigDecimal.valueOf(days).multiply(dailyRate(vehicleCategory));
    }

    private BigDecimal dailyRate(VehicleCategory vehicleCategory) {
        return switch (vehicleCategory) {
            case Compact -> pricing.compact();
            case Sedan -> pricing.sedan();
            case SUV -> pricing.suv();
            case Luxury -> pricing.luxury();
        };
    }
}
