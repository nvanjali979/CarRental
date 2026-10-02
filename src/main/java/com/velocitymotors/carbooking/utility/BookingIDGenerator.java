package com.velocitymotors.carbooking.utility;

import com.velocitymotors.carbooking.repository.BookingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingIDGenerator {

    @PersistenceContext
    private EntityManager entityManager;


    /**
     *  Generates BookingID according to Hibernate Sequence
     *  booking_id_seq
     */
    public String generateBookingID(){
        Number sequenceValue = (Number) entityManager
                .createNativeQuery("SELECT NEXT VALUE FOR booking_id_seq")
                .getSingleResult();
        return String.format("BKG%07d", sequenceValue.longValue());
    }
}
