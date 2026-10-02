package com.velocitymotors.carbooking.controller;

import com.velocitymotors.carbooking.domain.Booking;
import com.velocitymotors.carbooking.dto.BookingRequest;
import com.velocitymotors.carbooking.dto.BookingResponse;
import com.velocitymotors.carbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/confirm")
    public ResponseEntity confirmBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        BookingResponse response = bookingService.confirmBooking(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity getBooking(@PathVariable String bookingId) {
        Booking booking = bookingService.getBooking(bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(booking);
    }
}
