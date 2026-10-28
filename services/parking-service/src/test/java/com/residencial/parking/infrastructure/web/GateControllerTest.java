package com.residencial.parking.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencial.parking.application.dto.*;
import com.residencial.parking.application.usecase.ListParkingSpotsUseCase;
import com.residencial.parking.application.usecase.RegisterVehicleEntryUseCase;
import com.residencial.parking.application.usecase.RegisterVehicleExitUseCase;
import com.residencial.parking.domain.exception.NoAvailableParkingSpotException;
import com.residencial.parking.domain.exception.PlateAlreadyActiveException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {GateController.class, GlobalExceptionHandler.class})
class GateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterVehicleEntryUseCase registerEntryUseCase;

    @MockBean
    private RegisterVehicleExitUseCase registerExitUseCase;

    @MockBean
    private ListParkingSpotsUseCase listParkingSpotsUseCase;

    @Test
    @DisplayName("POST /api/v1/parking/entries returns 201 Created with spot assigned")
    void registerEntryReturns201() throws Exception {
        UUID houseId = UUID.randomUUID();
        UUID guardUserId = UUID.randomUUID();
        RegisterVehicleEntryRequest request = new RegisterVehicleEntryRequest(
                "P-123XYZ",
                houseId,
                "A-101",
                "Carlos Guest",
                "ID-9999",
                guardUserId
        );

        UUID entryId = UUID.randomUUID();
        VehicleEntryResponse response = new VehicleEntryResponse(
                entryId,
                "P-123XYZ",
                houseId,
                "A-101",
                "Carlos Guest",
                "ID-9999",
                UUID.randomUUID(),
                "P-04",
                guardUserId,
                Instant.now(),
                null,
                null,
                "ACTIVE",
                false,
                Instant.now()
        );

        when(registerEntryUseCase.execute(any(RegisterVehicleEntryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/parking/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(entryId.toString()))
                .andExpect(jsonPath("$.plate").value("P-123XYZ"))
                .andExpect(jsonPath("$.spotNumber").value("P-04"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/parking/entries returns 409 Conflict when vehicle is already active")
    void registerEntryDuplicatePlateReturns409() throws Exception {
        RegisterVehicleEntryRequest request = new RegisterVehicleEntryRequest(
                "P-123XYZ",
                UUID.randomUUID(),
                "A-101",
                "Carlos Guest",
                "ID-9999",
                UUID.randomUUID()
        );

        when(registerEntryUseCase.execute(any(RegisterVehicleEntryRequest.class)))
                .thenThrow(new PlateAlreadyActiveException("P-123XYZ", "P-04"));

        mockMvc.perform(post("/api/v1/parking/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Plate Already Active In Parking"))
                .andExpect(jsonPath("$.errorCode").value("PLATE_ALREADY_ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/parking/entries returns 409 Conflict when no parking spots are available")
    void registerEntryNoSpotsReturns409() throws Exception {
        RegisterVehicleEntryRequest request = new RegisterVehicleEntryRequest(
                "P-123XYZ",
                UUID.randomUUID(),
                "A-101",
                "Carlos Guest",
                "ID-9999",
                UUID.randomUUID()
        );

        when(registerEntryUseCase.execute(any(RegisterVehicleEntryRequest.class)))
                .thenThrow(new NoAvailableParkingSpotException());

        mockMvc.perform(post("/api/v1/parking/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("No Available Parking Spot"))
                .andExpect(jsonPath("$.errorCode").value("NO_AVAILABLE_PARKING_SPOT"));
    }

    @Test
    @DisplayName("POST /api/v1/parking/entries/{id}/exit returns 200 OK")
    void registerExitByIdReturns200() throws Exception {
        UUID entryId = UUID.randomUUID();
        VehicleExitResponse response = new VehicleExitResponse(
                entryId,
                "P-123XYZ",
                "P-04",
                Instant.now().minusSeconds(3600),
                Instant.now(),
                60L,
                "1 hours, 0 minutes",
                false,
                "COMPLETED"
        );

        when(registerExitUseCase.executeById(eq(entryId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/parking/entries/{id}/exit", entryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entryId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.durationMinutes").value(60));
    }

    @Test
    @DisplayName("GET /api/v1/parking/spots returns 200 OK with list of spots")
    void listSpotsReturns200() throws Exception {
        ParkingSpotStatusResponse spot1 = new ParkingSpotStatusResponse(
                UUID.randomUUID(),
                "P-01",
                "VISITOR",
                false,
                null
        );

        when(listParkingSpotsUseCase.execute()).thenReturn(List.of(spot1));

        mockMvc.perform(get("/api/v1/parking/spots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].spotNumber").value("P-01"))
                .andExpect(jsonPath("$[0].isOccupied").value(false));
    }
}
