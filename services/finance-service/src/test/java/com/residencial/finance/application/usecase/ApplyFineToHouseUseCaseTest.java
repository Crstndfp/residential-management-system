package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.ApplyFineRequest;
import com.residencial.finance.application.dto.ApplyFineResponse;
import com.residencial.finance.domain.exception.FineTypeNotFoundException;
import com.residencial.finance.domain.model.Fine;
import com.residencial.finance.domain.model.FineStatus;
import com.residencial.finance.domain.model.FineType;
import com.residencial.finance.domain.repository.FineRepository;
import com.residencial.finance.domain.repository.FineTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplyFineToHouseUseCaseTest {

    @Mock
    private FineRepository fineRepository;

    @Mock
    private FineTypeRepository fineTypeRepository;

    private ApplyFineToHouseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ApplyFineToHouseUseCase(fineRepository, fineTypeRepository);
    }

    @Test
    @DisplayName("US-203: Successfully apply fine using default amount from fine type")
    void shouldApplyFineWithDefaultAmountSuccessfully() {
        UUID houseId = UUID.randomUUID();
        FineType fineType = new FineType("NOISE_VIOLATION", "Ruidos Excesivos", "Musica alta", new BigDecimal("300.00"));

        ApplyFineRequest request = new ApplyFineRequest(
                houseId,
                "M-01 L-05",
                "NOISE_VIOLATION",
                null, // Use default amount
                "Música a alto volumen después de medianoche",
                "https://storage.example.com/evidence/noise-01.jpg",
                UUID.randomUUID()
        );

        when(fineTypeRepository.findByCode("NOISE_VIOLATION")).thenReturn(Optional.of(fineType));
        when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplyFineResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals(houseId, response.houseId());
        assertEquals("M-01 L-05", response.houseNumber());
        assertEquals("NOISE_VIOLATION", response.fineTypeCode());
        assertEquals(new BigDecimal("300.00"), response.amount());
        assertEquals(FineStatus.PENDING, response.status());

        verify(fineRepository).save(any(Fine.class));
    }

    @Test
    @DisplayName("US-203: Successfully apply fine with custom amount override")
    void shouldApplyFineWithCustomAmount() {
        UUID houseId = UUID.randomUUID();
        FineType fineType = new FineType("SPEEDING", "Exceso de Velocidad", "Manejo rapido", new BigDecimal("500.00"));

        ApplyFineRequest request = new ApplyFineRequest(
                houseId,
                "M-02 L-11",
                "SPEEDING",
                new BigDecimal("750.00"), // Custom amount for severe offense
                "Conducir a 60 km/h en zona infantil",
                null,
                null
        );

        when(fineTypeRepository.findByCode("SPEEDING")).thenReturn(Optional.of(fineType));
        when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplyFineResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("750.00"), response.amount());
        verify(fineRepository).save(any(Fine.class));
    }

    @Test
    @DisplayName("US-203: Reject fine when fine type code does not exist")
    void shouldRejectWhenFineTypeNotFound() {
        ApplyFineRequest request = new ApplyFineRequest(
                UUID.randomUUID(),
                "M-01 L-01",
                "INVALID_FINE_CODE",
                null,
                "Motivo",
                null,
                null
        );

        when(fineTypeRepository.findByCode("INVALID_FINE_CODE")).thenReturn(Optional.empty());

        assertThrows(FineTypeNotFoundException.class, () -> useCase.execute(request));
        verify(fineRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-203: Reject fine when fine type exists but is inactive")
    void shouldRejectWhenFineTypeIsInactive() {
        FineType inactiveType = mock(FineType.class);
        when(inactiveType.isActive()).thenReturn(false);

        when(fineTypeRepository.findByCode("DEPRECATED_FINE")).thenReturn(Optional.of(inactiveType));

        ApplyFineRequest request = new ApplyFineRequest(
                UUID.randomUUID(),
                "M-01 L-01",
                "DEPRECATED_FINE",
                null,
                "Motivo",
                null,
                null
        );

        assertThrows(FineTypeNotFoundException.class, () -> useCase.execute(request));
        verify(fineRepository, never()).save(any());
    }
}
