package com.velocitymotors.carbooking.exception;

import java.time.Instant;
import java.util.List;

public record ExceptionMessage(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<String> details
) {
}
