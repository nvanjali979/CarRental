package com.velocitymotors.carbooking.dto;

import java.math.BigDecimal;

public record BankTransferPaymentEvent(String paymentId, String senderAccountNumber,
                                       BigDecimal paymentAmount, String transactionDetails) {

    private static final String VARIABLE = " ";

    public String extractBookingID() {
        return transactionDetails.split(VARIABLE)[1];
    }
}
