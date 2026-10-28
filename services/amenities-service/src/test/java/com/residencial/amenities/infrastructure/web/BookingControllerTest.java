package com.residencial.amenities.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencial.amenities.application.dto.BookingResponse;
import com.residencial.amenities.application.dto.CreateBookingRequest;
import com.residencial.amenities.application.usecase.CreateBookingUseCase;
import com.residencial.amenities.domain.exception.BookingConflictException;
import com.residencial.amenities.domain.exception.DelinquentHouseException;
import com.residencial.amenities.domain.model.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {BookingController.class, GlobalExceptionHandler.class})
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateBookingUseCase createBookingUseCase;

    @Test
    @DisplayName("US-302 Web: Successfully create booking")
    void shouldCreateBookingSuccessfully() throws Exception {
        UUID bookingId = UUID.randomUUID();
        UUID amenityId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(2);

        BookingResponse response = new BookingResponse(
                bookingId,
                amenityId,
                "Casa Club",
                houseId,
                "A-101",
                UUID.randomUUID(),
                "Carlos Gómez",
                "BIRTHDAY",
                date,
                LocalTime.of(15, 0),
                LocalTime.of(20, 0),
                BookingStatus.CONFIRMED,
                new BigDecimal("500.00"),
                new BigDecimal("300.00"),
                UUID.randomUUID(),
                Instant.now()
        );

        when(createBookingUseCase.execute(any(CreateBookingRequest.class))).thenReturn(response);

        CreateBookingRequest request = new CreateBookingRequest(
                amenityId,
                houseId,
                "A-101",
                UUID.randomUUID(),
                "Carlos Gómez",
                "BIRTHDAY",
                date,
                LocalTime.of(15, 0),
                LocalTime.of(20, 0)
        );

        mockMvc.perform(post("/api/v1/amenities/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bookingId.toString()))
                .andExpect(jsonPath("$.houseNumber").value("A-101"))
                .andExpect(jsonPath("$.totalCost").value(500.00))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("US-302 Web: Reject delinquent house with 403 Forbidden")
    void shouldRejectDelinquentHouse() throws Exception {
        UUID amenityId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();

        when(createBookingUseCase.execute(any(CreateBookingRequest.class)))
                .thenThrow(new DelinquentHouseException("B-202"));

        CreateBookingRequest request = new CreateBookingRequest(
                amenityId,
                houseId,
                "B-202",
                UUID.randomUUID(),
                "Moroso",
                "BIRTHDAY",
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0),
                LocalTime.of(14, 0)
        );

        mockMvc.perform(post("/api/v1/amenities/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("House Delinquent - Booking Forbidden"))
                .andExpect(jsonPath("$.errorCode").value("HOUSE_DELINQUENT_RESERVATION_BLOCKED"));
    }

    @Test
    @DisplayName("US-302 Web: Reject conflicting booking time slot with 409 Conflict")
    void shouldRejectConflict() throws Exception {
        when(createBookingUseCase.execute(any(CreateBookingRequest.class)))
                .thenThrow(new BookingConflictException("Slot occupied"));

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "C-303",
                UUID.randomUUID(),
                "Vecino",
                "BIRTHDAY",
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0),
                LocalTime.of(14, 0)
        );

        mockMvc.perform(post("/api/v1/amenities/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Booking Slot Conflict"))
                .andExpect(jsonPath("$.errorCode").value("BOOKING_SLOT_CONFLICT"));
    }
}
