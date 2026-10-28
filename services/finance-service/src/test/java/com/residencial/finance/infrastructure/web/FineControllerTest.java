package com.residencial.finance.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencial.finance.application.dto.ApplyFineRequest;
import com.residencial.finance.application.dto.ApplyFineResponse;
import com.residencial.finance.application.usecase.ApplyFineToHouseUseCase;
import com.residencial.finance.domain.exception.FineTypeNotFoundException;
import com.residencial.finance.domain.model.FineStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {FineController.class, GlobalExceptionHandler.class})
class FineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplyFineToHouseUseCase applyFineToHouseUseCase;

    @Test
    @DisplayName("US-203 Web: Successfully apply fine to house")
    void shouldApplyFineSuccessfully() throws Exception {
        UUID fineId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();
        ApplyFineResponse response = new ApplyFineResponse(
                fineId,
                houseId,
                "A-101",
                "NOISE_VIOLATION",
                "Ruidos Excesivos Fuera de Horario",
                new BigDecimal("300.00"),
                "Música a alto volumen a la 1:00 AM",
                "https://s3.amazonaws.com/evidence/noise-1.jpg",
                FineStatus.PENDING,
                Instant.now()
        );

        when(applyFineToHouseUseCase.execute(any(ApplyFineRequest.class))).thenReturn(response);

        ApplyFineRequest request = new ApplyFineRequest(
                houseId,
                "A-101",
                "NOISE_VIOLATION",
                new BigDecimal("300.00"),
                "Música a alto volumen a la 1:00 AM",
                "https://s3.amazonaws.com/evidence/noise-1.jpg",
                UUID.randomUUID()
        );

        mockMvc.perform(post("/api/v1/finance/fines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fineId").value(fineId.toString()))
                .andExpect(jsonPath("$.houseNumber").value("A-101"))
                .andExpect(jsonPath("$.fineTypeCode").value("NOISE_VIOLATION"))
                .andExpect(jsonPath("$.amount").value(300.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("US-203 Web: Return 400 when missing required fields")
    void shouldReturnBadRequestWhenMissingFields() throws Exception {
        ApplyFineRequest invalidRequest = new ApplyFineRequest(
                null, // missing houseId
                "",   // blank houseNumber
                "",   // blank fineTypeCode
                null,
                "",   // blank reason
                null,
                null
        );

        mockMvc.perform(post("/api/v1/finance/fines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    @DisplayName("US-203 Web: Return 400 when fine type is not found")
    void shouldReturnBadRequestWhenFineTypeNotFound() throws Exception {
        when(applyFineToHouseUseCase.execute(any(ApplyFineRequest.class)))
                .thenThrow(new FineTypeNotFoundException("UNKNOWN_TYPE"));

        ApplyFineRequest request = new ApplyFineRequest(
                UUID.randomUUID(),
                "B-202",
                "UNKNOWN_TYPE",
                null,
                "Infracción desconocida",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/finance/fines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("FINE_TYPE_NOT_FOUND"));
    }

    @Test
    @DisplayName("TC-203-02: Successfully apply fine with custom amount override")
    void shouldApplyFineWithCustomAmountSuccessfully() throws Exception {
        UUID fineId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();
        ApplyFineResponse response = new ApplyFineResponse(
                fineId,
                houseId,
                "C-102",
                "SPEEDING",
                "Exceso de Velocidad",
                new BigDecimal("750.00"),
                "Conducción temeraria",
                null,
                FineStatus.PENDING,
                Instant.now()
        );

        when(applyFineToHouseUseCase.execute(any(ApplyFineRequest.class))).thenReturn(response);

        ApplyFineRequest request = new ApplyFineRequest(
                houseId,
                "C-102",
                "SPEEDING",
                new BigDecimal("750.00"),
                "Conducción temeraria",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/finance/fines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fineId").value(fineId.toString()))
                .andExpect(jsonPath("$.houseNumber").value("C-102"))
                .andExpect(jsonPath("$.amount").value(750.00));
    }

    @Test
    @DisplayName("TC-203-05: Reject custom amount less than or equal to zero returns 400 Bad Request")
    void shouldRejectNonPositiveCustomAmount() throws Exception {
        ApplyFineRequest invalidRequest = new ApplyFineRequest(
                UUID.randomUUID(),
                "C-101",
                "NOISE_VIOLATION",
                new BigDecimal("-50.00"), // Negative custom amount
                "Motivo",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/finance/fines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors.customAmount").exists());
    }
}
