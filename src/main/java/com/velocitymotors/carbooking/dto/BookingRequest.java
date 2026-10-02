package com.velocitymotors.carbooking.dto;

import com.velocitymotors.carbooking.domain.PaymentMode;
import com.velocitymotors.carbooking.domain.VehicleCategory;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


public record BookingRequest(
        @NotBlank(message = "Customer Name is required")
        String customerName,

        @NotBlank(message = "Vehicle ID is required")
        String vehicleId,

        @NotNull(message = "Rental Start Date is required")
        @FutureOrPresent(message = "Rental Start Date must not be in the past")
        LocalDate rentalStartDate,

        @NotNull(message = "Rental End Date is required")
        LocalDate rentalEndDate,

        @NotNull(message = "Vehicle Category is required")
        VehicleCategory vehicleCategory,

        @NotNull(message = "Payment Mode is required")
        PaymentMode paymentMode,

        String paymentReference

) {
}
