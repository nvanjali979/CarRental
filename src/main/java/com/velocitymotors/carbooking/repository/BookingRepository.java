package com.velocitymotors.carbooking.repository;

import com.velocitymotors.carbooking.domain.Booking;
import com.velocitymotors.carbooking.domain.BookingStatus;
import com.velocitymotors.carbooking.domain.PaymentMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findByBookingStatusAndPaymentMode(BookingStatus bookingStatus, PaymentMode paymentMode);
}
