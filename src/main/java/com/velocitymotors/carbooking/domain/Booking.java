package com.velocitymotors.carbooking.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Booking {

    @Id
    @Column(name = "booking_id")
    String BookingId;
    @Column(name = "customer_name", nullable = false, unique = true, length = 10)
    String customerName;

    @Column(name = "vehicle_id", nullable = false)
    String vehicleId;

    @Column(name = "rental_start_date", nullable = false)
    LocalDate rentalStartDate;

    @Column(name = "rental_end_date", nullable = false)
    LocalDate rentalEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_category", nullable = false)
    VehicleCategory vehicleCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false)
    PaymentMode paymentMode;

    String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    BookingStatus bookingStatus;

    @Column(name = "total_amount", nullable = false)
    BigDecimal totalAmount;

    public String getBookingId() {
        return BookingId;
    }

    public void setBookingId(String bookingId) {
        BookingId = bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDate getRentalStartDate() {
        return rentalStartDate;
    }

    public void setRentalStartDate(LocalDate rentalStartDate) {
        this.rentalStartDate = rentalStartDate;
    }

    public LocalDate getRentalEndDate() {
        return rentalEndDate;
    }

    public void setRentalEndDate(LocalDate rentalEndDate) {
        this.rentalEndDate = rentalEndDate;
    }

    public VehicleCategory getVehicleCategory() {
        return vehicleCategory;
    }

    public void setVehicleCategory(VehicleCategory vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    protected Booking() {
    }

    public Booking(String bookingId, String customerName, String vehicleId, LocalDate rentalStartDate, LocalDate rentalEndDate, VehicleCategory vehicleCategory, PaymentMode paymentMode, String paymentReference, BookingStatus bookingStatus, BigDecimal totalAmount) {
        BookingId = bookingId;
        this.customerName = customerName;
        this.vehicleId = vehicleId;
        this.rentalStartDate = rentalStartDate;
        this.rentalEndDate = rentalEndDate;
        this.vehicleCategory = vehicleCategory;
        this.paymentMode = paymentMode;
        this.paymentReference = paymentReference;
        this.bookingStatus = bookingStatus;
        this.totalAmount = totalAmount;
    }
}
