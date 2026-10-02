package com.velocitymotors.carbooking.controller;

import com.jayway.jsonpath.JsonPath;
import com.velocitymotors.carbooking.client.CreditCardValidationClient;
import com.velocitymotors.carbooking.client.PaymentStatusResponse;
import com.velocitymotors.carbooking.domain.PaymentMode;
import com.velocitymotors.carbooking.domain.VehicleCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "security.api-key=test-api-key")
class BookingControllerIntegrationTest {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_VALUE = "test-api-key";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreditCardValidationClient creditCardValidationClient;

    private String requestJson(String customerName, String vehicleId, PaymentMode paymentMode, String paymentReference) {
        String start = LocalDate.now().plusDays(1).toString();
        String end = LocalDate.now().plusDays(3).toString();
        return """
                {
                  "customerName": %s,
                  "vehicleId": "%s",
                  "rentalStartDate": "%s",
                  "rentalEndDate": "%s",
                  "vehicleCategory": "%s",
                  "paymentMode": "%s",
                  "paymentReference": %s
                }
                """.formatted(
                quoteOrNull(customerName), vehicleId, start, end,
                VehicleCategory.Sedan.name(), paymentMode.name(), quoteOrNull(paymentReference));
    }

    private String quoteOrNull(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    @Test
    void confirmBooking_cashPaymentWithValidRequest_returns201WithConfirmedStatus() throws Exception {
        String json = requestJson("Integ1", "NL100001", PaymentMode.CASH, null);

        mockMvc.perform(post("/api/v1/bookings/confirm")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.bookingID").exists());
    }

    @Test
    void confirmBooking_withoutApiKey_returns401() throws Exception {
        String json = requestJson("Integ2", "NL100002", PaymentMode.CASH, null);

        mockMvc.perform(post("/api/v1/bookings/confirm")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void confirmBooking_withWrongApiKey_returns401() throws Exception {
        String json = requestJson("Integ3", "NL100003", PaymentMode.CASH, null);

        mockMvc.perform(post("/api/v1/bookings/confirm")
                        .header(API_KEY_HEADER, "wrong-key")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void confirmBooking_missingRequiredField_returns400() throws Exception {
        String json = requestJson(null, "NL100004", PaymentMode.CASH, null);

        mockMvc.perform(post("/api/v1/bookings/confirm")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmBooking_invalidVehicleId_returns404() throws Exception {
        String json = requestJson("Integ4", "INVALID", PaymentMode.CASH, null);

        mockMvc.perform(post("/api/v1/bookings/confirm")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirmBooking_creditCardDeclined_returns422() throws Exception {
        when(creditCardValidationClient.validate(any())).thenReturn(new PaymentStatusResponse("2026-01-01", "DECLINED"));
        String json = requestJson("Integ5", "NL100005", PaymentMode.CREDIT_CARD, "PAYREF1");

        mockMvc.perform(post("/api/v1/bookings/confirm")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void confirmBooking_bankTransfer_returnsPendingPaymentStatus() throws Exception {
        String json = requestJson("Integ6", "NL100006", PaymentMode.BANK_TRANSFER, null);

        mockMvc.perform(post("/api/v1/bookings/confirm")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingStatus").value("PENDING_PAYMENT"));
    }

    @Test
    void getBooking_afterConfirming_returnsBookingDetails() throws Exception {
        String json = requestJson("Integ7", "NL100007", PaymentMode.CASH, null);

        String response = mockMvc.perform(post("/api/v1/bookings/confirm")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String bookingId = JsonPath.read(response, "$.bookingID");

        mockMvc.perform(get("/api/v1/bookings/" + bookingId)
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Integ7"))
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));
    }

    @Test
    void getBooking_unknownBookingId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/UNKNOWN-ID")
                        .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound());
    }
}
