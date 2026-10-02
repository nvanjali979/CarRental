package com.velocitymotors.carbooking.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BankTransferPaymentEventTest {

    @Test
    void extractBookingID_returnsSecondTokenOfTransactionDetails() {
        BankTransferPaymentEvent event = new BankTransferPaymentEvent(
                "PAY1", "NL91ABNA0417164300", BigDecimal.valueOf(150), "Booking BKG0000042 payment");

        assertThat(event.extractBookingID()).isEqualTo("BKG0000042");
    }

    @Test
    void extractBookingID_withOnlyBookingIdAndPrefix_returnsBookingId() {
        BankTransferPaymentEvent event = new BankTransferPaymentEvent(
                "PAY2", "NL91ABNA0417164300", BigDecimal.valueOf(30), "Ref BKG0000001");

        assertThat(event.extractBookingID()).isEqualTo("BKG0000001");
    }
}
