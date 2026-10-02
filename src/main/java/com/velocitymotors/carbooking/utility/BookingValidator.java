package com.velocitymotors.carbooking.utility;

import com.velocitymotors.carbooking.exception.InvalidBookingException;
import com.velocitymotors.carbooking.exception.InvalidVehicleIDException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class BookingValidator {

    @Value("${car-booking.max-rent-days}")
    private long MAX_RENT_DAYS;
    private static final String VEHICLE_ID_MATCHER = "^NL\\d{6}$";

    public void validateBookingDates(LocalDate rentalStartDate, LocalDate rentalEndDate) {
        if(!rentalEndDate.isAfter(rentalStartDate)) throw new InvalidBookingException("Rental End Date should be after Rental Start Date");
        long days = ChronoUnit.DAYS.between(rentalStartDate, rentalEndDate);
        if(days > MAX_RENT_DAYS){
            throw new InvalidBookingException("Vehicle cannot be on rent for more than "+MAX_RENT_DAYS+ " days");
        }
    }

    public void validateVehicleId(String vehicleId) {
        if(!vehicleId.matches(VEHICLE_ID_MATCHER))
            throw new InvalidVehicleIDException("Invalid Vehicle ID");
    }
}
