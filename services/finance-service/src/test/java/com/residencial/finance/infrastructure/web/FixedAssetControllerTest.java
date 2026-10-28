package com.residencial.finance.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencial.finance.application.dto.FixedAssetDto;
import com.residencial.finance.application.dto.RegisterAssetRequest;
import com.residencial.finance.application.dto.RegisterAssetResponse;
import com.residencial.finance.application.dto.UpdateAssetStatusRequest;
import com.residencial.finance.application.usecase.ListFixedAssetsUseCase;
import com.residencial.finance.application.usecase.RegisterFixedAssetUseCase;
import com.residencial.finance.application.usecase.UpdateAssetStatusUseCase;
import com.residencial.finance.domain.exception.AssetNotFoundException;
import com.residencial.finance.domain.exception.DuplicateAssetCodeException;
import com.residencial.finance.domain.model.AssetCategory;
import com.residencial.finance.domain.model.AssetStatus;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {FixedAssetController.class, GlobalExceptionHandler.class})
class FixedAssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterFixedAssetUseCase registerFixedAssetUseCase;

    @MockBean
    private UpdateAssetStatusUseCase updateAssetStatusUseCase;

    @MockBean
    private ListFixedAssetsUseCase listFixedAssetsUseCase;

    @Test
    @DisplayName("US-205 Web: Successfully register new fixed asset")
    void shouldRegisterAssetSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        RegisterAssetResponse response = new RegisterAssetResponse(
                id,
                "ACT-CHAIR-001",
                "Silla Plegable Casa Club",
                AssetCategory.CLUBHOUSE_FURNITURE,
                "Casa Club - Salón Principal",
                new BigDecimal("250.00"),
                LocalDate.of(2026, 9, 1),
                AssetStatus.EXCELLENT,
                "Lote de 50 sillas",
                Instant.now()
        );

        when(registerFixedAssetUseCase.execute(any(RegisterAssetRequest.class))).thenReturn(response);

        RegisterAssetRequest request = new RegisterAssetRequest(
                "ACT-CHAIR-001",
                "Silla Plegable Casa Club",
                AssetCategory.CLUBHOUSE_FURNITURE,
                "Casa Club - Salón Principal",
                new BigDecimal("250.00"),
                LocalDate.of(2026, 9, 1),
                AssetStatus.EXCELLENT,
                "Lote de 50 sillas"
        );

        mockMvc.perform(post("/api/v1/finance/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("ACT-CHAIR-001"))
                .andExpect(jsonPath("$.name").value("Silla Plegable Casa Club"))
                .andExpect(jsonPath("$.category").value("CLUBHOUSE_FURNITURE"))
                .andExpect(jsonPath("$.acquisitionCost").value(250.00));
    }

    @Test
    @DisplayName("US-205 Web: Reject duplicate asset code with 409 Conflict")
    void shouldReturnConflictOnDuplicateCode() throws Exception {
        when(registerFixedAssetUseCase.execute(any(RegisterAssetRequest.class)))
                .thenThrow(new DuplicateAssetCodeException("ACT-CHAIR-001"));

        RegisterAssetRequest request = new RegisterAssetRequest(
                "ACT-CHAIR-001",
                "Silla Duplicada",
                AssetCategory.CLUBHOUSE_FURNITURE,
                "Casa Club",
                new BigDecimal("250.00"),
                LocalDate.now(),
                AssetStatus.EXCELLENT,
                null
        );

        mockMvc.perform(post("/api/v1/finance/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate Asset Code"))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_ASSET_CODE"));
    }

    @Test
    @DisplayName("US-205 Web: Successfully update asset status")
    void shouldUpdateStatusSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        FixedAssetDto updatedDto = new FixedAssetDto(
                id,
                "ACT-BOMB-001",
                "Bomba de Agua",
                AssetCategory.FACILITY_EQUIPMENT,
                "Pozo",
                new BigDecimal("12000.00"),
                LocalDate.of(2026, 1, 1),
                AssetStatus.DAMAGED,
                "Motor quemado",
                Instant.now(),
                Instant.now()
        );

        when(updateAssetStatusUseCase.execute(eq(id), any(UpdateAssetStatusRequest.class))).thenReturn(updatedDto);

        UpdateAssetStatusRequest request = new UpdateAssetStatusRequest(
                AssetStatus.DAMAGED,
                "Motor quemado por sobrecarga eléctrica",
                UUID.randomUUID()
        );

        mockMvc.perform(patch("/api/v1/finance/assets/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("DAMAGED"));
    }

    @Test
    @DisplayName("US-205 Web: Return 404 when updating non-existent asset")
    void shouldReturnNotFoundOnNonExistentAsset() throws Exception {
        UUID randomId = UUID.randomUUID();
        when(updateAssetStatusUseCase.execute(eq(randomId), any(UpdateAssetStatusRequest.class)))
                .thenThrow(new AssetNotFoundException("Not found"));

        UpdateAssetStatusRequest request = new UpdateAssetStatusRequest(
                AssetStatus.NEEDS_MAINTENANCE,
                "Mantenimiento",
                null
        );

        mockMvc.perform(patch("/api/v1/finance/assets/" + randomId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Asset Not Found"));
    }

    @Test
    @DisplayName("US-205 Web: List assets with optional filters")
    void shouldListAssets() throws Exception {
        FixedAssetDto asset1 = new FixedAssetDto(
                UUID.randomUUID(), "ACT-01", "Bomba", AssetCategory.FACILITY_EQUIPMENT,
                "Pozo", new BigDecimal("5000.00"), LocalDate.now(), AssetStatus.EXCELLENT,
                null, Instant.now(), Instant.now()
        );

        when(listFixedAssetsUseCase.execute(AssetCategory.FACILITY_EQUIPMENT, null))
                .thenReturn(List.of(asset1));

        mockMvc.perform(get("/api/v1/finance/assets")
                        .param("category", "FACILITY_EQUIPMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("ACT-01"));
    }
}
