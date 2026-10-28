package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.FixedAssetDto;
import com.residencial.finance.application.dto.UpdateAssetStatusRequest;
import com.residencial.finance.domain.exception.AssetNotFoundException;
import com.residencial.finance.domain.model.AssetCategory;
import com.residencial.finance.domain.model.AssetStatus;
import com.residencial.finance.domain.model.AssetStatusHistory;
import com.residencial.finance.domain.model.FixedAsset;
import com.residencial.finance.domain.repository.AssetStatusHistoryRepository;
import com.residencial.finance.domain.repository.FixedAssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAssetStatusUseCaseTest {

    @Mock
    private FixedAssetRepository fixedAssetRepository;

    @Mock
    private AssetStatusHistoryRepository assetStatusHistoryRepository;

    private UpdateAssetStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateAssetStatusUseCase(fixedAssetRepository, assetStatusHistoryRepository);
    }

    @Test
    @DisplayName("US-205: Update asset status and record audit history entry")
    void shouldUpdateStatusAndRecordHistory() {
        UUID assetId = UUID.randomUUID();
        FixedAsset asset = new FixedAsset(
                "ACT-MOW-002",
                "Cortadora de Césped Honda",
                AssetCategory.MAINTENANCE_TOOL,
                "Bodega de Jardinería",
                new BigDecimal("4800.00"),
                LocalDate.of(2026, 5, 10),
                AssetStatus.EXCELLENT,
                null
        );

        when(fixedAssetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(fixedAssetRepository.save(any(FixedAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAssetStatusRequest request = new UpdateAssetStatusRequest(
                AssetStatus.NEEDS_MAINTENANCE,
                "Falla en el carburador detectada durante mantenimiento rutinario",
                UUID.randomUUID()
        );

        FixedAssetDto result = useCase.execute(assetId, request);

        assertNotNull(result);
        assertEquals(AssetStatus.NEEDS_MAINTENANCE, result.status());
        verify(assetStatusHistoryRepository).save(any(AssetStatusHistory.class));
        verify(fixedAssetRepository).save(asset);
    }

    @Test
    @DisplayName("US-205 Invariant: Reject update for non-existent asset")
    void shouldThrowWhenAssetNotFound() {
        UUID randomId = UUID.randomUUID();
        when(fixedAssetRepository.findById(randomId)).thenReturn(Optional.empty());

        UpdateAssetStatusRequest request = new UpdateAssetStatusRequest(
                AssetStatus.DAMAGED,
                "Roto",
                null
        );

        assertThrows(AssetNotFoundException.class, () -> useCase.execute(randomId, request));
        verify(assetStatusHistoryRepository, never()).save(any());
        verify(fixedAssetRepository, never()).save(any());
    }
}
