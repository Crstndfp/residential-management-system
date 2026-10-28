package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.RegisterAssetRequest;
import com.residencial.finance.application.dto.RegisterAssetResponse;
import com.residencial.finance.domain.exception.DuplicateAssetCodeException;
import com.residencial.finance.domain.model.AssetCategory;
import com.residencial.finance.domain.model.AssetStatus;
import com.residencial.finance.domain.model.FixedAsset;
import com.residencial.finance.domain.repository.FixedAssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterFixedAssetUseCaseTest {

    @Mock
    private FixedAssetRepository fixedAssetRepository;

    private RegisterFixedAssetUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterFixedAssetUseCase(fixedAssetRepository);
    }

    @Test
    @DisplayName("US-205: Successfully register a new fixed asset")
    void shouldRegisterAssetSuccessfully() {
        RegisterAssetRequest request = new RegisterAssetRequest(
                "ACT-BOMB-001",
                "Bomba de Agua Sumergible 5HP",
                AssetCategory.FACILITY_EQUIPMENT,
                "Pozo Principal Cisterna",
                new BigDecimal("12500.00"),
                LocalDate.of(2026, 10, 1),
                AssetStatus.EXCELLENT,
                "Garantía de 2 años con Hidroequipos S.A."
        );

        when(fixedAssetRepository.existsByCode("ACT-BOMB-001")).thenReturn(false);
        when(fixedAssetRepository.save(any(FixedAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterAssetResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals("ACT-BOMB-001", response.code());
        assertEquals("Bomba de Agua Sumergible 5HP", response.name());
        assertEquals(AssetCategory.FACILITY_EQUIPMENT, response.category());
        assertEquals(AssetStatus.EXCELLENT, response.status());
        assertEquals(new BigDecimal("12500.00"), response.acquisitionCost());
        verify(fixedAssetRepository).save(any(FixedAsset.class));
    }

    @Test
    @DisplayName("US-205 Invariant: Reject duplicate asset code")
    void shouldRejectDuplicateAssetCode() {
        RegisterAssetRequest request = new RegisterAssetRequest(
                "act-bomb-001",
                "Bomba Repetida",
                AssetCategory.FACILITY_EQUIPMENT,
                "Cisterna",
                new BigDecimal("5000.00"),
                LocalDate.now(),
                AssetStatus.EXCELLENT,
                null
        );

        when(fixedAssetRepository.existsByCode("ACT-BOMB-001")).thenReturn(true);

        assertThrows(DuplicateAssetCodeException.class, () -> useCase.execute(request));
        verify(fixedAssetRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-205 Invariant: Reject null request")
    void shouldRejectNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }
}
