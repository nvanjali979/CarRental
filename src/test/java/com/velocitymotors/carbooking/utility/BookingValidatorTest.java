package com.velocitymotors.carbooking.utility;

import com.velocitymotors.carbooking.exception.InvalidBookingException;
import com.velocitymotors.carbooking.exception.InvalidVehicleIDException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingValidatorTest {

    private static final long MAX_RENT_DAYS = 21;

    private BookingValidator bookingValidator;

    @BeforeEach
    void setUp() {
        bookingValidator = new BookingValidator();
        ReflectionTestUtils.setField(bookingValidator, "MAX_RENT_DAYS", MAX_RENT_DAYS);
    }

    @Test
    void validateBookingDates_endAfterStartWithinLimit_doesNotThrow() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(5);

        assertThatCode(() -> bookingValidator.validateBookingDates(start, end)).doesNotThrowAnyException();
    }

    @Test
    void validateBookingDates_endEqualsStart_throwsInvalidBookingException() {
        LocalDate date = LocalDate.now();

        assertThatThrownBy(() -> bookingValidator.validateBookingDates(date, date))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("after");
    }

    @Test
    void validateBookingDates_endBeforeStart_throwsInvalidBookingException() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);

        assertThatThrownBy(() -> bookingValidator.validateBookingDates(start, end))
                .isInstanceOf(InvalidBookingException.class);
    }

    @Test
    void validateBookingDates_exceedsMaxRentDays_throwsInvalidBookingException() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(MAX_RENT_DAYS + 1);

        assertThatThrownBy(() -> bookingValidator.validateBookingDates(start, end))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining(String.valueOf(MAX_RENT_DAYS));
    }

    @Test
    void validateBookingDates_exactlyMaxRentDays_doesNotThrow() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(MAX_RENT_DAYS);

        assertThatCode(() -> bookingValidator.validateBookingDates(start, end)).doesNotThrowAnyException();
    }

    @Test
    void validateVehicleId_validFormat_doesNotThrow() {
        assertThatCode(() -> bookingValidator.validateVehicleId("NL123456")).doesNotThrowAnyException();
    }

    @Test
    void validateVehicleId_tooFewDigits_throwsInvalidVehicleIDException() {
        assertThatThrownBy(() -> bookingValidator.validateVehicleId("NL12345"))
                .isInstanceOf(InvalidVehicleIDException.class);
    }

    @Test
    void validateVehicleId_wrongPrefix_throwsInvalidVehicleIDException() {
        assertThatThrownBy(() -> bookingValidator.validateVehicleId("XX123456"))
                .isInstanceOf(InvalidVehicleIDException.class);
    }

    @Test
    void validateVehicleId_lowerCasePrefix_throwsInvalidVehicleIDException() {
        assertThatThrownBy(() -> bookingValidator.validateVehicleId("nl123456"))
                .isInstanceOf(InvalidVehicleIDException.class);
    }
}
