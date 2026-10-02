package com.velocitymotors.carbooking.utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingIDGeneratorTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private Query query;

    private BookingIDGenerator bookingIDGenerator;

    @BeforeEach
    void setUp() {
        bookingIDGenerator = new BookingIDGenerator();
        ReflectionTestUtils.setField(bookingIDGenerator, "entityManager", entityManager);
    }

    @Test
    void generateBookingID_padsSequenceValueWithLeadingZeros() {
        when(entityManager.createNativeQuery("SELECT NEXT VALUE FOR booking_id_seq")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(42L);

        String bookingId = bookingIDGenerator.generateBookingID();

        assertThat(bookingId).isEqualTo("BKG0000042");
    }

    @Test
    void generateBookingID_largeSequenceValue_isNotTruncated() {
        when(entityManager.createNativeQuery("SELECT NEXT VALUE FOR booking_id_seq")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(12345678L);

        String bookingId = bookingIDGenerator.generateBookingID();

        assertThat(bookingId).isEqualTo("BKG12345678");
    }
}
