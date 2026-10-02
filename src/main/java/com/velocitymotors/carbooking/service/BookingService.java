package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.dto.BookingRequest;
import com.velocitymotors.carbooking.dto.BookingResponse;
import com.velocitymotors.carbooking.client.CreditCardValidationClient;
import com.velocitymotors.carbooking.domain.Booking;
import com.velocitymotors.carbooking.domain.BookingStatus;
import com.velocitymotors.carbooking.exception.BookingNotFoundException;
import com.velocitymotors.carbooking.exception.PaymentDeclinedException;
import com.velocitymotors.carbooking.repository.BookingRepository;
import com.velocitymotors.carbooking.client.PaymentStatusRetrievalRequest;
import com.velocitymotors.carbooking.domain.PaymentStatus;
import com.velocitymotors.carbooking.client.PaymentStatusResponse;
import com.velocitymotors.carbooking.utility.BookingIDGenerator;
import com.velocitymotors.carbooking.utility.BookingValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class BookingService {

    private static Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final BookingValidator bookingValidator;
    private final PricingService pricingService;
    private final BookingIDGenerator bookingIDGenerator;
    private final CreditCardValidationClient creditCardValidationClient;

    @Autowired
    public BookingService(BookingRepository bookingRepository, BookingValidator bookingValidator,
                          PricingService pricingService, BookingIDGenerator bookingIDGenerator,
                          CreditCardValidationClient creditCardValidationClient) {
        this.bookingRepository = bookingRepository;
        this.bookingValidator = bookingValidator;
        this.pricingService = pricingService;
        this.bookingIDGenerator = bookingIDGenerator;
        this.creditCardValidationClient = creditCardValidationClient;
    }

    @Transactional
    public BookingResponse confirmBooking(BookingRequest bookingRequest) {
        validateRequest(bookingRequest);
        updateBookingStatus(bookingRequest);
        Booking booking = persistBookinginDB(bookingRequest);
        return new BookingResponse(booking.getBookingId(), booking.getBookingStatus());
    }

    private Booking persistBookinginDB(BookingRequest bookingRequest) {
        String bookingId = bookingIDGenerator.generateBookingID();
        BigDecimal totalAmount = pricingService.calculateAmount(bookingRequest.vehicleCategory(), bookingRequest.rentalStartDate(), bookingRequest.rentalEndDate());
        BookingStatus bookingStatus = updateBookingStatus(bookingRequest);

        Booking booking = new Booking(bookingId, bookingRequest.customerName(), bookingRequest.vehicleId(), bookingRequest.rentalStartDate(),
                bookingRequest.rentalEndDate(), bookingRequest.vehicleCategory(), bookingRequest.paymentMode(), bookingRequest.paymentReference(), bookingStatus, totalAmount);
        return bookingRepository.save(booking);
    }

    private BookingStatus updateBookingStatus(BookingRequest bookingRequest) {
        return switch (bookingRequest.paymentMode()) {
            case DIGITAL_WALLET -> BookingStatus.CONFIRMED;
            case CASH -> BookingStatus.CONFIRMED;
            case CREDIT_CARD -> {
                //Call REST API of credit-card-validation-service
                PaymentStatusResponse response = creditCardValidationClient.validate(new PaymentStatusRetrievalRequest(bookingRequest.paymentReference()));
                if (response.status().equals(PaymentStatus.APPROVED.name()))
                    yield BookingStatus.CONFIRMED;
                else throw new PaymentDeclinedException("Payment with reference number "
                        + bookingRequest.paymentReference() + " was declined");

            }
            case BANK_TRANSFER -> BookingStatus.PENDING_PAYMENT;
        };
    }

    private void validateRequest(BookingRequest bookingRequest) {
        bookingValidator.validateBookingDates(bookingRequest.rentalStartDate(), bookingRequest.rentalEndDate());
        bookingValidator.validateVehicleId(bookingRequest.vehicleId());
    }

    @Transactional
    public void bankTransferPayment(String bookingID, BigDecimal totalAmount) {
        bookingRepository.findById(bookingID).ifPresentOrElse(booking -> {
                    if (booking.getBookingStatus().equals(BookingStatus.PENDING_PAYMENT) &&
                            booking.getTotalAmount().equals(totalAmount)) {
                        booking.setBookingStatus(BookingStatus.CONFIRMED);
                        logger.info("Booking {} is confirmed", booking.getBookingId());
                    } else {
                        logger.info("Invalid Payment");
                        return;
                    }
                },
                () -> new BookingNotFoundException("Booking not found " + bookingID)
        );
    }

    public Booking getBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }
}
