package com.velocitymotors.carbooking.kafka;

import com.velocitymotors.carbooking.dto.BankTransferPaymentEvent;
import com.velocitymotors.carbooking.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BankTransferPaymentEventListener {

    private static Logger logger = LoggerFactory.getLogger(BankTransferPaymentEventListener.class);
    private final BookingService bookingService;

    @Autowired
    public BankTransferPaymentEventListener(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @KafkaListener(topics = "${car-booking.kafka.bank-transfer-payment-events-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void bankTransferPaymentEvent(BankTransferPaymentEvent event) {
        logger.info("Booking ID : {}, Amount paid : {}", event.extractBookingID(), event.paymentAmount());
        bookingService.bankTransferPayment(event.extractBookingID(), event.paymentAmount());
    }

}
