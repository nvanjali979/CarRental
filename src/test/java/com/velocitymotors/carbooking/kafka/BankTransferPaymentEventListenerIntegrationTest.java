package com.velocitymotors.carbooking.kafka;

import com.velocitymotors.carbooking.domain.Booking;
import com.velocitymotors.carbooking.domain.BookingStatus;
import com.velocitymotors.carbooking.domain.PaymentMode;
import com.velocitymotors.carbooking.domain.VehicleCategory;
import com.velocitymotors.carbooking.repository.BookingRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Tests the Kafka consumer path end-to-end against a real broker started with Testcontainers
 */
@Testcontainers
@SpringBootTest
class BankTransferPaymentEventListenerIntegrationTest {

    @Container
    @ServiceConnection
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.0.0");

    @Autowired
    private BookingRepository bookingRepository;

    @Value("${car-booking.kafka.bank-transfer-payment-events-topic}")
    private String topic;

    private KafkaTemplate<String, String> producer;

    private KafkaTemplate<String, String> producer() {
        if (producer == null) {
            Map<String, Object> producerProps = new HashMap<>();
            producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            producer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));
        }
        return producer;
    }

    @AfterEach
    void tearDown() {
        if (producer != null) {
            producer.destroy();
            producer = null;
        }
    }

    private void publishBankTransferEvent(String paymentId, BigDecimal amount, String transactionId) {
        String eventJson = """
                {
                  "paymentId": "%s",
                  "senderAccountNumber": "NL91ABNA0417164300",
                  "paymentAmount": %s,
                  "transactionDetails": "%s"
                }
                """.formatted(paymentId, amount, transactionId);
        producer().send(topic, eventJson);
    }

    @Test
    void bankTransferPaymentEvent_withMatchingAmount_confirmsPendingBooking() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        Booking booking = new Booking("BKG0000001", "John", "NL123454", start, end,
                VehicleCategory.Sedan, PaymentMode.BANK_TRANSFER, null, BookingStatus.PENDING_PAYMENT, BigDecimal.valueOf(30));
        bookingRepository.save(booking);

        publishBankTransferEvent("PAY001", BigDecimal.valueOf(30), "TXN987654321 BKG0000001");

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(bookingRepository.findById("BKG0000001"))
                        .isPresent()
                        .get()
                        .extracting(Booking::getBookingStatus)
                        .isEqualTo(BookingStatus.CONFIRMED));
    }

    @Test
    void bankTransferPaymentEvent_withMismatchedAmount_leavesBookingPending() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
        Booking booking = new Booking("BKG0000002", "Noah", "NL900002", start, end,
                VehicleCategory.Sedan, PaymentMode.BANK_TRANSFER, null, BookingStatus.PENDING_PAYMENT, BigDecimal.valueOf(30));
        bookingRepository.save(booking);

        publishBankTransferEvent("PAY002", BigDecimal.valueOf(999), "TXN987654321 BKG0000002");

        // A follow-up confirmed booking on the same topic gives the (intentionally) wrong-amount
        // message time to be consumed before we assert it had no effect.
        Booking marker = new Booking("BKG0000003", "KafkaIT3", "NL900003", start, end,
                VehicleCategory.Sedan, PaymentMode.BANK_TRANSFER, null, BookingStatus.PENDING_PAYMENT, BigDecimal.valueOf(45));
        bookingRepository.save(marker);
        publishBankTransferEvent("PAY-IT-3", BigDecimal.valueOf(45), "TXN987654321 BKG0000003");

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(bookingRepository.findById("BKG0000003"))
                        .isPresent()
                        .get()
                        .extracting(Booking::getBookingStatus)
                        .isEqualTo(BookingStatus.CONFIRMED));

        assertThat(bookingRepository.findById("BKG0000002"))
                .isPresent()
                .get()
                .extracting(Booking::getBookingStatus)
                .isEqualTo(BookingStatus.PENDING_PAYMENT);
    }
}
