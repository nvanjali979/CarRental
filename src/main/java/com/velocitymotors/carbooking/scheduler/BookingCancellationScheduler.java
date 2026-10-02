package com.velocitymotors.carbooking.scheduler;

import com.velocitymotors.carbooking.domain.Booking;
import com.velocitymotors.carbooking.domain.BookingStatus;
import com.velocitymotors.carbooking.domain.PaymentMode;
import com.velocitymotors.carbooking.repository.BookingRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Component
public class BookingCancellationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BookingCancellationScheduler.class);

    private final BookingRepository bookingRepository;
    private final ZoneId zoneId;
    private final long hours;

    @Autowired
    public BookingCancellationScheduler(BookingRepository bookingRepository,
                                        @Value("${car-booking.rental.zone-id}") String zoneIdString,
                                        @Value("${car-booking.rental.minBooking-hours}") int hours
    ) {
        this.bookingRepository = bookingRepository;
        this.zoneId = ZoneId.of(zoneIdString);
        this.hours = hours;
    }

    @Scheduled(fixedRateString = "${car-booking.cancellation.check-interval}")
    @Transactional
    public void cancelTransferBookingUnpaid() {

        List<Booking> bookings = bookingRepository.findByBookingStatusAndPaymentMode(BookingStatus.PENDING_PAYMENT, PaymentMode.BANK_TRANSFER);
        bookings.forEach(booking -> {
            Instant rentalStartInstant = booking.getRentalStartDate().atStartOfDay(zoneId).toInstant();
            if (Duration.between(Instant.now(), rentalStartInstant).getSeconds() < hours * 60 * 60) {
                booking.setBookingStatus(BookingStatus.CANCELLED);
                logger.info("Booking {} cancelled as no payment was made before the due date ", booking.getBookingId());
            }
        });

    }
}
