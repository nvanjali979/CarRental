package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.client.CreditCardValidationClient;
import com.velocitymotors.carbooking.client.PaymentStatusResponse;
import com.velocitymotors.carbooking.domain.Booking;
import com.velocitymotors.carbooking.domain.BookingStatus;
import com.velocitymotors.carbooking.domain.PaymentMode;
import com.velocitymotors.carbooking.domain.VehicleCategory;
import com.velocitymotors.carbooking.dto.BookingRequest;
import com.velocitymotors.carbooking.dto.BookingResponse;
import com.velocitymotors.carbooking.exception.BookingNotFoundException;
import com.velocitymotors.carbooking.exception.PaymentDeclinedException;
import com.velocitymotors.carbooking.repository.BookingRepository;
import com.velocitymotors.carbooking.utility.BookingIDGenerator;
import com.velocitymotors.carbooking.utility.BookingValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingValidator bookingValidator;
    @Mock
    private PricingService pricingService;
    @Mock
    private BookingIDGenerator bookingIDGenerator;
    @Mock
    private CreditCardValidationClient creditCardValidationClient;

    private BookingService bookingService;

    private static final LocalDate START_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate END_DATE = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository, bookingValidator, pricingService,
                bookingIDGenerator, creditCardValidationClient);
    }

    private BookingRequest requestWith(PaymentMode paymentMode, String paymentReference) {
        return new BookingRequest("John", "NL123456", START_DATE, END_DATE,
                VehicleCategory.Sedan, paymentMode, paymentReference);
    }

    private void stubCommonCollaborators() {
        when(bookingIDGenerator.generateBookingID()).thenReturn("BKG0000001");
        when(pricingService.calculateAmount(any(), any(), any())).thenReturn(BigDecimal.valueOf(30));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void confirmBooking_cashPayment_isConfirmedImmediately() {
        stubCommonCollaborators();
        BookingRequest request = requestWith(PaymentMode.CASH, null);

        BookingResponse response = bookingService.confirmBooking(request);

        assertThat(response.bookingID()).isEqualTo("BKG0000001");
        assertThat(response.bookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingValidator).validateBookingDates(START_DATE, END_DATE);
        verify(bookingValidator).validateVehicleId("NL123456");
    }

    @Test
    void confirmBooking_digitalWalletPayment_isConfirmedImmediately() {
        stubCommonCollaborators();
        BookingRequest request = requestWith(PaymentMode.DIGITAL_WALLET, null);

        BookingResponse response = bookingService.confirmBooking(request);

        assertThat(response.bookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void confirmBooking_bankTransferPayment_isPendingPayment() {
        stubCommonCollaborators();
        BookingRequest request = requestWith(PaymentMode.BANK_TRANSFER, null);

        BookingResponse response = bookingService.confirmBooking(request);

        assertThat(response.bookingStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void confirmBooking_creditCardApproved_isConfirmed() {
        stubCommonCollaborators();
        when(creditCardValidationClient.validate(any())).thenReturn(new PaymentStatusResponse("2024-01-01", "APPROVED"));
        BookingRequest request = requestWith(PaymentMode.CREDIT_CARD, "PAY123");

        BookingResponse response = bookingService.confirmBooking(request);

        assertThat(response.bookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void confirmBooking_creditCardDeclined_throwsPaymentDeclinedException() {
        when(creditCardValidationClient.validate(any())).thenReturn(new PaymentStatusResponse("2024-01-01", "DECLINED"));
        BookingRequest request = requestWith(PaymentMode.CREDIT_CARD, "PAY123");

        assertThatThrownBy(() -> bookingService.confirmBooking(request))
                .isInstanceOf(PaymentDeclinedException.class)
                .hasMessageContaining("PAY123");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void confirmBooking_invalidatesRequestBeforePersisting() {
        org.mockito.Mockito.doThrow(new com.velocitymotors.carbooking.exception.InvalidVehicleIDException("Invalid Vehicle ID"))
                .when(bookingValidator).validateVehicleId(anyString());
        BookingRequest request = requestWith(PaymentMode.CASH, null);

        assertThatThrownBy(() -> bookingService.confirmBooking(request))
                .isInstanceOf(com.velocitymotors.carbooking.exception.InvalidVehicleIDException.class);

        verify(bookingRepository, never()).save(any());
        verify(pricingService, never()).calculateAmount(any(), any(), any());
    }

    @Test
    void persistedBooking_containsValuesFromRequestAndCalculatedAmount() {
        stubCommonCollaborators();
        BookingRequest request = requestWith(PaymentMode.CASH, null);

        bookingService.confirmBooking(request);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking saved = captor.getValue();
        assertThat(saved.getBookingId()).isEqualTo("BKG0000001");
        assertThat(saved.getCustomerName()).isEqualTo("John");
        assertThat(saved.getVehicleId()).isEqualTo("NL123456");
        assertThat(saved.getTotalAmount()).isEqualTo(BigDecimal.valueOf(30));
        assertThat(saved.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void bankTransferPayment_matchingAmountOnPendingBooking_confirmsBooking() {
        Booking booking = new Booking("BKG0000001", "John", "NL123456", START_DATE, END_DATE,
                VehicleCategory.Sedan, PaymentMode.BANK_TRANSFER, null, BookingStatus.PENDING_PAYMENT, BigDecimal.valueOf(30));
        when(bookingRepository.findById("BKG0000001")).thenReturn(java.util.Optional.of(booking));

        bookingService.bankTransferPayment("BKG0000001", BigDecimal.valueOf(30));

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void bankTransferPayment_amountMismatch_leavesBookingPending() {
        Booking booking = new Booking("BKG0000001", "John", "NL123456", START_DATE, END_DATE,
                VehicleCategory.Sedan, PaymentMode.BANK_TRANSFER, null, BookingStatus.PENDING_PAYMENT, BigDecimal.valueOf(30));
        when(bookingRepository.findById("BKG0000001")).thenReturn(java.util.Optional.of(booking));

        bookingService.bankTransferPayment("BKG0000001", BigDecimal.valueOf(999));

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void bankTransferPayment_alreadyConfirmedBooking_isNotReConfirmed() {
        Booking booking = new Booking("BKG0000001", "John", "NL123456", START_DATE, END_DATE,
                VehicleCategory.Sedan, PaymentMode.BANK_TRANSFER, null, BookingStatus.CONFIRMED, BigDecimal.valueOf(30));
        when(bookingRepository.findById("BKG0000001")).thenReturn(java.util.Optional.of(booking));

        bookingService.bankTransferPayment("BKG0000001", BigDecimal.valueOf(30));

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void getBooking_existingBooking_returnsBooking() {
        Booking booking = new Booking("BKG0000001", "John", "NL123456", START_DATE, END_DATE,
                VehicleCategory.Sedan, PaymentMode.CASH, null, BookingStatus.CONFIRMED, BigDecimal.valueOf(30));
        when(bookingRepository.findById("BKG0000001")).thenReturn(java.util.Optional.of(booking));

        Booking result = bookingService.getBooking("BKG0000001");

        assertThat(result).isSameAs(booking);
    }

    @Test
    void getBooking_unknownBooking_throwsBookingNotFoundException() {
        when(bookingRepository.findById("UNKNOWN")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> bookingService.getBooking("UNKNOWN"))
                .isInstanceOf(BookingNotFoundException.class);
    }
}
