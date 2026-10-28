package com.residencial.amenities.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencial.amenities.application.dto.*;
import com.residencial.amenities.application.usecase.CreateMaintenanceTicketUseCase;
import com.residencial.amenities.application.usecase.GetTicketDetailUseCase;
import com.residencial.amenities.application.usecase.ListTicketsUseCase;
import com.residencial.amenities.application.usecase.UpdateTicketProgressUseCase;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TicketController.class, GlobalExceptionHandler.class})
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateMaintenanceTicketUseCase createTicketUseCase;

    @MockBean
    private UpdateTicketProgressUseCase updateTicketProgressUseCase;

    @MockBean
    private GetTicketDetailUseCase getTicketDetailUseCase;

    @MockBean
    private ListTicketsUseCase listTicketsUseCase;

    @Test
    @DisplayName("POST /api/v1/amenities/tickets returns 201 Created")
    void createTicketReturns201() throws Exception {
        UUID reporterUserId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();

        CreateTicketRequest request = new CreateTicketRequest(
                "Broken Lamp",
                "LIGHTING",
                "Park 2",
                "Lamp flickers at night",
                "https://storage.local/lamp.jpg",
                "MEDIUM",
                reporterUserId,
                "Alice",
                "OWNER",
                houseId,
                "B-101"
        );

        UUID ticketId = UUID.randomUUID();
        TicketResponse response = new TicketResponse(
                ticketId,
                "TCK-12345678",
                request.title(),
                request.category(),
                request.location(),
                request.description(),
                request.photoUrl(),
                "OPEN",
                "MEDIUM",
                null,
                reporterUserId,
                "Alice",
                "OWNER",
                houseId,
                "B-101",
                Instant.now(),
                Instant.now(),
                null
        );

        when(createTicketUseCase.execute(any(CreateTicketRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/amenities/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.folio").value("TCK-12345678"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("GET /api/v1/amenities/tickets returns 200 OK with list")
    void listTicketsReturns200() throws Exception {
        TicketResponse response = new TicketResponse(
                UUID.randomUUID(),
                "TCK-12345678",
                "Broken Lamp",
                "LIGHTING",
                "Park 2",
                "Desc",
                null,
                "OPEN",
                "MEDIUM",
                null,
                UUID.randomUUID(),
                "Alice",
                "OWNER",
                UUID.randomUUID(),
                "B-101",
                Instant.now(),
                Instant.now(),
                null
        );

        when(listTicketsUseCase.execute(any(), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/amenities/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].folio").value("TCK-12345678"));
    }

    @Test
    @DisplayName("GET /api/v1/amenities/tickets/{id} returns 200 OK with detail and history")
    void getTicketDetailReturns200() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketResponse ticket = new TicketResponse(
                ticketId,
                "TCK-12345678",
                "Broken Lamp",
                "LIGHTING",
                "Park 2",
                "Desc",
                null,
                "OPEN",
                "MEDIUM",
                null,
                UUID.randomUUID(),
                "Alice",
                "OWNER",
                UUID.randomUUID(),
                "B-101",
                Instant.now(),
                Instant.now(),
                null
        );

        TicketDetailResponse detail = new TicketDetailResponse(ticket, List.of());
        when(getTicketDetailUseCase.execute(ticketId)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/amenities/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticket.folio").value("TCK-12345678"));
    }

    @Test
    @DisplayName("PATCH /api/v1/amenities/tickets/{id}/progress returns 200 OK")
    void updateProgressReturns200() throws Exception {
        UUID ticketId = UUID.randomUUID();
        UpdateTicketProgressRequest request = new UpdateTicketProgressRequest(
                UUID.randomUUID(),
                "Admin Staff",
                "ADMIN",
                "IN_PROGRESS",
                "Tech Mario",
                "Assigned technician Mario to replace bulb"
        );

        TicketResponse ticket = new TicketResponse(
                ticketId,
                "TCK-12345678",
                "Broken Lamp",
                "LIGHTING",
                "Park 2",
                "Desc",
                null,
                "IN_PROGRESS",
                "MEDIUM",
                "Tech Mario",
                UUID.randomUUID(),
                "Alice",
                "OWNER",
                UUID.randomUUID(),
                "B-101",
                Instant.now(),
                Instant.now(),
                null
        );

        TicketDetailResponse detail = new TicketDetailResponse(ticket, List.of());
        when(updateTicketProgressUseCase.execute(eq(ticketId), any(UpdateTicketProgressRequest.class))).thenReturn(detail);

        mockMvc.perform(patch("/api/v1/amenities/tickets/{id}/progress", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticket.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.ticket.assignedTechnician").value("Tech Mario"));
    }
}
