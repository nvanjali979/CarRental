package com.velocitymotors.carbooking.dto;

import com.velocitymotors.carbooking.domain.BookingStatus;

public record BookingResponse(
        String bookingID,
        BookingStatus bookingStatus
) {
}
