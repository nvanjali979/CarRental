package com.velocitymotors.carbooking.repository;

import com.velocitymotors.carbooking.domain.Booking;
import com.velocitymotors.carbooking.domain.BookingStatus;
import com.velocitymotors.carbooking.domain.PaymentMode;
import com.velocitymotors.carbooking.domain.VehicleCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void findByBookingStatusAndPaymentMode_returnsOnlyMatchingBookings() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);

        bookingRepository.save(new Booking("BKG0000001", "Alice", "NL111111", start, end,
                VehicleCategory.Sedan, PaymentMode.BANK_TRANSFER, null, BookingStatus.PENDING_PAYMENT, BigDecimal.valueOf(30)));
        bookingRepository.save(new Booking("BKG0000002", "Bob", "NL222222", start, end,
                VehicleCategory.Sedan, PaymentMode.BANK_TRANSFER, null, BookingStatus.CONFIRMED, BigDecimal.valueOf(30)));
        bookingRepository.save(new Booking("BKG0000003", "Carl", "NL333333", start, end,
                VehicleCategory.Sedan, PaymentMode.CASH, null, BookingStatus.PENDING_PAYMENT, BigDecimal.valueOf(30)));

        List<Booking> result = bookingRepository.findByBookingStatusAndPaymentMode(BookingStatus.PENDING_PAYMENT, PaymentMode.BANK_TRANSFER);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookingId()).isEqualTo("BKG0000001");
    }

    @Test
    void findByBookingStatusAndPaymentMode_noMatches_returnsEmptyList() {
        List<Booking> result = bookingRepository.findByBookingStatusAndPaymentMode(BookingStatus.CANCELLED, PaymentMode.BANK_TRANSFER);

        assertThat(result).isEmpty();
    }

    @Test
    void save_thenFindById_returnsPersistedBooking() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(2);
        Booking booking = new Booking("BKG0000010", "Dave", "NL444444", start, end,
                VehicleCategory.Luxury, PaymentMode.CASH, null, BookingStatus.CONFIRMED, BigDecimal.valueOf(25));

        bookingRepository.save(booking);

        assertThat(bookingRepository.findById("BKG0000010"))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getCustomerName()).isEqualTo("Dave");
                    assertThat(found.getVehicleCategory()).isEqualTo(VehicleCategory.Luxury);
                });
    }
}
