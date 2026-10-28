package com.residencial.parking.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencial.parking.application.dto.ApplyParkingFineRequest;
import com.residencial.parking.application.dto.CovertResidentAlertResponse;
import com.residencial.parking.application.dto.ParkingFineResponse;
import com.residencial.parking.application.usecase.ApplyParkingFineUseCase;
import com.residencial.parking.application.usecase.DetectCovertResidentsUseCase;
import com.residencial.parking.domain.repository.ParkingFineRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(controllers = {ParkingAnalyticsController.class, GlobalExceptionHandler.class})
class ParkingAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DetectCovertResidentsUseCase detectCovertResidentsUseCase;

    @MockBean
    private ApplyParkingFineUseCase applyParkingFineUseCase;

    @MockBean
    private ParkingFineRepository fineRepository;

    @Test
    @DisplayName("GET /api/v1/parking/analytics/covert-residents returns 200 OK with suspect alert")
    void getCovertResidentsReturns200() throws Exception {
        CovertResidentAlertResponse suspect = new CovertResidentAlertResponse(
                "P-123XYZ",
                UUID.randomUUID(),
                "A-101",
                4L,
                true,
                "SOSPECHA DE RESIDENTE ENCUBIERTO",
                "HIGH",
                Instant.now().minusSeconds(86400 * 5),
                Instant.now().minusSeconds(86400),
                List.of()
        );

        when(detectCovertResidentsUseCase.execute(eq(7))).thenReturn(List.of(suspect));

        mockMvc.perform(get("/api/v1/parking/analytics/covert-residents?days=7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].plate").value("P-123XYZ"))
                .andExpect(jsonPath("$[0].overnightCount").value(4))
                .andExpect(jsonPath("$[0].isSuspect").value(true))
                .andExpect(jsonPath("$[0].label").value("SOSPECHA DE RESIDENTE ENCUBIERTO"));
    }

    @Test
    @DisplayName("POST /api/v1/parking/fines returns 201 Created")
    void applyFineReturns201() throws Exception {
        UUID houseId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        ApplyParkingFineRequest request = new ApplyParkingFineRequest(
                "P-123XYZ",
                houseId,
                "A-101",
                new BigDecimal("50.00"),
                "Uso indebido de parqueo de visitas",
                adminId,
                "Admin Staff"
        );

        ParkingFineResponse response = new ParkingFineResponse(
                UUID.randomUUID(),
                "P-123XYZ",
                houseId,
                "A-101",
                new BigDecimal("50.00"),
                "Uso indebido de parqueo de visitas",
                "Evidence summary",
                adminId,
                "Admin Staff",
                UUID.randomUUID(),
                "APPLIED",
                Instant.now()
        );

        when(applyParkingFineUseCase.execute(any(ApplyParkingFineRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/parking/fines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plate").value("P-123XYZ"))
                .andExpect(jsonPath("$.fineAmount").value(50.00))
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }
}
