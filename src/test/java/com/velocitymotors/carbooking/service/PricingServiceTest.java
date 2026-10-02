package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.config.VehiclePricingProperties;
import com.velocitymotors.carbooking.domain.VehicleCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        VehiclePricingProperties pricing = new VehiclePricingProperties(
                BigDecimal.valueOf(10), BigDecimal.valueOf(15), BigDecimal.valueOf(20), BigDecimal.valueOf(25));
        pricingService = new PricingService(pricing);
    }

    @Test
    void calculateAmount_compact_multipliesDailyRateByNumberOfDays() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 4);

        BigDecimal amount = pricingService.calculateAmount(VehicleCategory.Compact, start, end);

        assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    @Test
    void calculateAmount_sedan_usesSedanDailyRate() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 3);

        BigDecimal amount = pricingService.calculateAmount(VehicleCategory.Sedan, start, end);

        assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    @Test
    void calculateAmount_suv_usesSuvDailyRate() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 2);

        BigDecimal amount = pricingService.calculateAmount(VehicleCategory.SUV, start, end);

        assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(20));
    }

    @Test
    void calculateAmount_luxury_usesLuxuryDailyRate() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 5);

        BigDecimal amount = pricingService.calculateAmount(VehicleCategory.Luxury, start, end);

        assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void calculateAmount_sameStartAndEndDate_returnsZero() {
        LocalDate date = LocalDate.of(2026, 1, 1);

        BigDecimal amount = pricingService.calculateAmount(VehicleCategory.Compact, date, date);

        assertThat(amount).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
