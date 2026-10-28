package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.ReconciliationSummaryResponse;
import com.residencial.finance.domain.model.BankTransaction;
import com.residencial.finance.domain.model.ChargeStatus;
import com.residencial.finance.domain.model.MaintenanceCharge;
import com.residencial.finance.domain.repository.BankTransactionRepository;
import com.residencial.finance.domain.repository.MaintenanceChargeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconcileBankPaymentsUseCaseTest {

    @Mock
    private MaintenanceChargeRepository maintenanceChargeRepository;

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    private ReconcileBankPaymentsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReconcileBankPaymentsUseCase(maintenanceChargeRepository, bankTransactionRepository);
    }

    @Test
    @DisplayName("US-202: Successfully reconcile bank payments from CSV report")
    void shouldReconcileCsvPaymentsSuccessfully() {
        String csvContent = "Authorization,HouseRef,Date,Amount\n" +
                "AUTH-1001,M-01 L-05,2026-10-05,650.00\n" +
                "AUTH-1002,M-01 L-06,2026-10-06,650.00\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bank_report_oct2026.csv",
                "text/csv",
                csvContent.getBytes()
        );

        MaintenanceCharge charge1 = new MaintenanceCharge(
                UUID.randomUUID(), "M-01 L-05", 2026, 10, new BigDecimal("650.00"), LocalDate.of(2026, 10, 31)
        );

        when(bankTransactionRepository.existsByAuthorizationNumber("AUTH-1001")).thenReturn(false);
        when(bankTransactionRepository.existsByAuthorizationNumber("AUTH-1002")).thenReturn(false);

        when(maintenanceChargeRepository.findPendingByHouseNumber("M-01 L-05")).thenReturn(Optional.of(charge1));
        when(maintenanceChargeRepository.findPendingByHouseNumber("M-01 L-06")).thenReturn(Optional.empty()); // unmatched

        ReconciliationSummaryResponse response = useCase.execute(file);

        assertNotNull(response);
        assertEquals(2, response.totalRowsRead());
        assertEquals(1, response.successfullyReconciled());
        assertEquals(0, response.duplicateTransactions());
        assertEquals(1, response.unmatchedTransactions());
        assertEquals(new BigDecimal("650.00"), response.totalAmountProcessed());
        assertEquals(ChargeStatus.PAID, charge1.getStatus());

        verify(maintenanceChargeRepository).save(charge1);
        verify(bankTransactionRepository, times(2)).save(any(BankTransaction.class));
    }

    @Test
    @DisplayName("US-202: Skip duplicate bank authorizations without failing the whole batch")
    void shouldSkipDuplicateAuthorizations() {
        String csvContent = "Authorization,HouseRef,Date,Amount\n" +
                "AUTH-DUP-1,M-01 L-05,2026-10-05,650.00\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bank_report.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(bankTransactionRepository.existsByAuthorizationNumber("AUTH-DUP-1")).thenReturn(true);

        ReconciliationSummaryResponse response = useCase.execute(file);

        assertNotNull(response);
        assertEquals(1, response.totalRowsRead());
        assertEquals(0, response.successfullyReconciled());
        assertEquals(1, response.duplicateTransactions());
        assertEquals(BigDecimal.ZERO, response.totalAmountProcessed());

        verify(maintenanceChargeRepository, never()).save(any());
        verify(bankTransactionRepository, never()).save(any());
    }
}
