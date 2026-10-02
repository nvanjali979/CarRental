package com.velocitymotors.carbooking.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private HttpServletRequest request;

    @Test
    void handleInvalidBooking_returns400WithMessage() {
        InvalidBookingException ex = new InvalidBookingException("Rental End Date should be after Rental Start Date");

        ResponseEntity<?> response = handler.handleInvalidBooking(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ExceptionMessage body = (ExceptionMessage) response.getBody();
        assertThat(body.message()).isEqualTo("Rental End Date should be after Rental Start Date");
        assertThat(body.status()).isEqualTo(400);
    }

    @Test
    void handleVehicleNotFound_returns404() {
        InvalidVehicleIDException ex = new InvalidVehicleIDException("Invalid Vehicle ID");

        ResponseEntity<?> response = handler.handleVehicleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ExceptionMessage body = (ExceptionMessage) response.getBody();
        assertThat(body.message()).isEqualTo("Invalid Vehicle ID");
    }

    @Test
    void handleBookingNotFound_returns404() {
        BookingNotFoundException ex = new BookingNotFoundException("BKG0000001");

        ResponseEntity<?> response = handler.handleBookingNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handlePaymentDeclined_returns422() {
        PaymentDeclinedException ex = new PaymentDeclinedException("Payment with reference number PAY123 was declined");

        ResponseEntity<?> response = handler.handlePaymentDeclined(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        ExceptionMessage body = (ExceptionMessage) response.getBody();
        assertThat(body.message()).contains("PAY123");
    }

    @Test
    void handleValidationServiceDown_returns503WithGenericMessage() {
        CreditCardValidationException ex = new CreditCardValidationException("Credit card Service is down", null);

        ResponseEntity<?> response = handler.handleValidationServiceDown(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        ExceptionMessage body = (ExceptionMessage) response.getBody();
        assertThat(body.message()).isEqualTo("Payment validation service is unavailable");
    }

    @Test
    void handleUnexpected_withPlainException_returns500() {
        lenient().when(request.getMethod()).thenReturn("GET");
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/bookings/confirm");
        RuntimeException ex = new RuntimeException("boom");

        ResponseEntity<?> response = handler.handleUnexpected(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ExceptionMessage body = (ExceptionMessage) response.getBody();
        assertThat(body.message()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void handleUnexpected_withErrorResponseException_usesItsStatusAndDetail() {
        ErrorResponseException ex = new ErrorResponseException(HttpStatus.CONFLICT);
        ex.setDetail("Conflicting booking state");

        ResponseEntity<?> response = handler.handleUnexpected(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ExceptionMessage body = (ExceptionMessage) response.getBody();
        assertThat(body.message()).isEqualTo("Conflicting booking state");
    }
}
