package com.residencial.amenities.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencial.amenities.application.dto.*;
import com.residencial.amenities.application.usecase.ConvokeAssemblyUseCase;
import com.residencial.amenities.application.usecase.CreateMotionUseCase;
import com.residencial.amenities.application.usecase.GetAssemblyQuorumUseCase;
import com.residencial.amenities.application.usecase.RegisterAttendanceUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AssemblyController.class, GlobalExceptionHandler.class})
class AssemblyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConvokeAssemblyUseCase convokeAssemblyUseCase;

    @MockBean
    private RegisterAttendanceUseCase registerAttendanceUseCase;

    @MockBean
    private GetAssemblyQuorumUseCase getAssemblyQuorumUseCase;

    @MockBean
    private CreateMotionUseCase createMotionUseCase;

    @Test
    @DisplayName("POST /api/v1/amenities/assemblies returns 201 Created")
    void convokeAssemblyReturns201() throws Exception {
        ConvokeAssemblyRequest request = new ConvokeAssemblyRequest(
                "Assembly 2026",
                "ORDINARY",
                Instant.now().plusSeconds(86400),
                "Agenda: Budget and Board",
                100
        );

        UUID assemblyId = UUID.randomUUID();
        AssemblyResponse response = new AssemblyResponse(
                assemblyId,
                request.title(),
                request.assemblyType(),
                request.scheduledAt(),
                request.agenda(),
                "CONVOKED",
                100,
                Instant.now(),
                Instant.now()
        );

        when(convokeAssemblyUseCase.execute(any(ConvokeAssemblyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/amenities/assemblies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(assemblyId.toString()))
                .andExpect(jsonPath("$.title").value("Assembly 2026"))
                .andExpect(jsonPath("$.status").value("CONVOKED"));
    }

    @Test
    @DisplayName("POST /api/v1/amenities/assemblies/{id}/attendance returns 200 OK")
    void registerAttendanceReturns200() throws Exception {
        UUID assemblyId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        RegisterAttendanceRequest request = new RegisterAttendanceRequest(
                houseId,
                "A-101",
                userId,
                "Alice Smith",
                true
        );

        AttendanceResponse response = new AttendanceResponse(
                UUID.randomUUID(),
                assemblyId,
                houseId,
                "A-101",
                userId,
                "Alice Smith",
                true,
                Instant.now()
        );

        when(registerAttendanceUseCase.execute(eq(assemblyId), any(RegisterAttendanceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/amenities/assemblies/{id}/attendance", assemblyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.houseNumber").value("A-101"))
                .andExpect(jsonPath("$.isPresent").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/amenities/assemblies/{id}/quorum returns 200 OK with quorum calculations")
    void getQuorumReturns200() throws Exception {
        UUID assemblyId = UUID.randomUUID();
        AssemblyQuorumResponse response = new AssemblyQuorumResponse(
                assemblyId,
                "Assembly 2026",
                "CONVOKED",
                100,
                55L,
                55.0,
                true
        );

        when(getAssemblyQuorumUseCase.execute(assemblyId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/amenities/assemblies/{id}/quorum", assemblyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResidentialHouses").value(100))
                .andExpect(jsonPath("$.presentHouses").value(55))
                .andExpect(jsonPath("$.quorumPercentage").value(55.0))
                .andExpect(jsonPath("$.hasQuorum").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/amenities/assemblies/{id}/motions returns 201 Created")
    void createMotionReturns201() throws Exception {
        UUID assemblyId = UUID.randomUUID();
        CreateMotionRequest request = new CreateMotionRequest(
                "Gym Renovation",
                "Approve gym renovation project"
        );

        UUID motionId = UUID.randomUUID();
        MotionResponse response = new MotionResponse(
                motionId,
                assemblyId,
                request.title(),
                request.description(),
                "PENDING",
                null,
                null,
                null,
                Instant.now()
        );

        when(createMotionUseCase.execute(eq(assemblyId), any(CreateMotionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/amenities/assemblies/{id}/motions", assemblyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(motionId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
