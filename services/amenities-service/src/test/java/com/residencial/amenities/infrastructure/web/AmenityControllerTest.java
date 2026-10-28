package com.residencial.amenities.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencial.amenities.application.dto.AmenityResponse;
import com.residencial.amenities.application.dto.CalendarSlotDto;
import com.residencial.amenities.application.dto.CreateAmenityRequest;
import com.residencial.amenities.application.dto.UpdateAmenityRequest;
import com.residencial.amenities.application.usecase.ConfigureAmenityUseCase;
import com.residencial.amenities.application.usecase.GetAmenityCalendarUseCase;
import com.residencial.amenities.application.usecase.ListAmenitiesUseCase;
import com.residencial.amenities.application.usecase.UpdateAmenityUseCase;
import com.residencial.amenities.domain.exception.DuplicateAmenityCodeException;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AmenityController.class, GlobalExceptionHandler.class})
class AmenityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConfigureAmenityUseCase configureAmenityUseCase;

    @MockBean
    private UpdateAmenityUseCase updateAmenityUseCase;

    @MockBean
    private ListAmenitiesUseCase listAmenitiesUseCase;

    @MockBean
    private GetAmenityCalendarUseCase getAmenityCalendarUseCase;

    @Test
    @DisplayName("US-301 Web: Successfully configure new amenity")
    void shouldCreateAmenitySuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        AmenityResponse response = new AmenityResponse(
                id,
                "POOL",
                "Piscina Semiolímpica",
                "Área de natación",
                30,
                new BigDecimal("50.00"),
                new BigDecimal("50.00"),
                LocalTime.of(7, 0),
                LocalTime.of(20, 0),
                "SWIMMING,TRAINING",
                true,
                Instant.now(),
                Instant.now()
        );

        when(configureAmenityUseCase.execute(any(CreateAmenityRequest.class))).thenReturn(response);

        CreateAmenityRequest request = new CreateAmenityRequest(
                "POOL",
                "Piscina Semiolímpica",
                "Área de natación",
                30,
                new BigDecimal("50.00"),
                new BigDecimal("50.00"),
                LocalTime.of(7, 0),
                LocalTime.of(20, 0),
                "SWIMMING,TRAINING"
        );

        mockMvc.perform(post("/api/v1/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("POOL"))
                .andExpect(jsonPath("$.rentalCost").value(50.00));
    }

    @Test
    @DisplayName("US-301 Web: Conflict on duplicate amenity code")
    void shouldReturnConflictOnDuplicateCode() throws Exception {
        when(configureAmenityUseCase.execute(any(CreateAmenityRequest.class)))
                .thenThrow(new DuplicateAmenityCodeException("POOL"));

        CreateAmenityRequest request = new CreateAmenityRequest(
                "POOL", "Piscina Duplicada", null, 10,
                BigDecimal.ZERO, BigDecimal.ZERO,
                LocalTime.of(8, 0), LocalTime.of(18, 0), "SWIM"
        );

        mockMvc.perform(post("/api/v1/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_AMENITY_CODE"));
    }

    @Test
    @DisplayName("US-302 Web: Get privacy-protected calendar")
    void shouldGetCalendarSlots() throws Exception {
        UUID id = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        CalendarSlotDto slot = new CalendarSlotDto(today, LocalTime.of(10, 0), LocalTime.of(14, 0), "Reservado");

        when(getAmenityCalendarUseCase.execute(eq(id), any(), any())).thenReturn(List.of(slot));

        mockMvc.perform(get("/api/v1/amenities/" + id + "/calendar")
                        .param("startDate", today.toString())
                        .param("endDate", today.plusDays(7).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].displayStatus").value("Reservado"));
    }
}
