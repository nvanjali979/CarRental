package com.velocitymotors.carbooking.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        ExceptionMessage body = new ExceptionMessage(Instant.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), "Request validation failed", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handleMalformedRequest(HttpMessageNotReadableException ex) {
        ExceptionMessage body = new ExceptionMessage(Instant.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), "Malformed request body", List.of(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity handleInvalidBooking(InvalidBookingException ex) {
        ExceptionMessage body = new ExceptionMessage(Instant.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidVehicleIDException.class)
    public ResponseEntity handleVehicleNotFound(InvalidVehicleIDException ex) {
        ExceptionMessage body = new ExceptionMessage(Instant.now(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity handleBookingNotFound(BookingNotFoundException ex) {
        ExceptionMessage body = new ExceptionMessage(Instant.now(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(PaymentDeclinedException.class)
    public ResponseEntity handlePaymentDeclined(PaymentDeclinedException ex) {
        ExceptionMessage body = new ExceptionMessage(Instant.now(), HttpStatus.UNPROCESSABLE_CONTENT.value(), HttpStatus.UNPROCESSABLE_CONTENT.getReasonPhrase(), ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(body);
    }

    @ExceptionHandler(CreditCardValidationException.class)
    public ResponseEntity handleValidationServiceDown(CreditCardValidationException ex) {
        log.error("credit-card-validation-service call failed", ex);
        ExceptionMessage body = new ExceptionMessage(Instant.now(), HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(), "Payment validation service is unavailable", List.of());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    // Catches all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity handleUnexpected(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            String detail = errorResponse.getBody().getDetail();
            ExceptionMessage body = new ExceptionMessage(
                    Instant.now(), status.value(), status.getReasonPhrase(), detail != null ? detail : ex.getMessage(), List.of());
            return ResponseEntity.status(status).body(body);
        }
        log.error("Unhandled exception while processing {} {}", request.getMethod(), request.getRequestURI(), ex);
        ExceptionMessage body = new ExceptionMessage(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "An unexpected error occurred", List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);

    }

    private ResponseEntity<ExceptionMessage> build(
            HttpStatus status, String message, List<String> details) {
        ExceptionMessage body = new ExceptionMessage(
                Instant.now(), status.value(), status.getReasonPhrase(), message, details);
        return ResponseEntity.status(status).body(body);
    }


}
