package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.AccountStatementResponse;
import com.residencial.finance.application.dto.SolvencyStatus;
import com.residencial.finance.domain.exception.TenantStatementAccessException;
import com.residencial.finance.domain.model.BankTransaction;
import com.residencial.finance.domain.model.BankTransactionStatus;
import com.residencial.finance.domain.model.Fine;
import com.residencial.finance.domain.model.FineType;
import com.residencial.finance.domain.model.MaintenanceCharge;
import com.residencial.finance.domain.repository.BankTransactionRepository;
import com.residencial.finance.domain.repository.FineRepository;
import com.residencial.finance.domain.repository.MaintenanceChargeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAccountStatementUseCaseTest {

    @Mock
    private MaintenanceChargeRepository maintenanceChargeRepository;

    @Mock
    private FineRepository fineRepository;

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    private GetAccountStatementUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAccountStatementUseCase(
                maintenanceChargeRepository,
                fineRepository,
                bankTransactionRepository
        );
    }

    @Test
    @DisplayName("US-204: Compute SOLVENT status when all charges are paid and no fines")
    void shouldReturnSolventWhenNoDebt() {
        UUID houseId = UUID.randomUUID();
        MaintenanceCharge paidCharge = new MaintenanceCharge(
                houseId, "A-101", 2026, 10, new BigDecimal("350.00"), LocalDate.now().minusDays(5)
        );
        paidCharge.applyPayment(new BigDecimal("350.00"));

        BankTransaction payment = new BankTransaction(
                "AUT-9988", "A-101", paidCharge, LocalDate.now().minusDays(5),
                new BigDecimal("350.00"), "report.xlsx", BankTransactionStatus.RECONCILED, null
        );

        when(maintenanceChargeRepository.findByHouseId(houseId)).thenReturn(List.of(paidCharge));
        when(fineRepository.findByHouseId(houseId)).thenReturn(Collections.emptyList());
        when(bankTransactionRepository.findByHouseReference("A-101")).thenReturn(List.of(payment));

        AccountStatementResponse response = useCase.execute(houseId, "OWNER");

        assertNotNull(response);
        assertEquals("A-101", response.houseNumber());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.totalBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.pendingMaintenanceBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.pendingFinesBalance()));
        assertEquals(0, new BigDecimal("350.00").compareTo(response.totalPaid()));
        assertEquals(SolvencyStatus.SOLVENT, response.solvencyStatus());
        assertEquals(2, response.movements().size());
    }

    @Test
    @DisplayName("US-204: Compute DELINQUENT status when maintenance charge is past due")
    void shouldReturnDelinquentWhenChargeOverdue() {
        UUID houseId = UUID.randomUUID();
        MaintenanceCharge overdueCharge = new MaintenanceCharge(
                houseId, "B-202", 2026, 9, new BigDecimal("350.00"), LocalDate.now().minusDays(10)
        );

        when(maintenanceChargeRepository.findByHouseId(houseId)).thenReturn(List.of(overdueCharge));
        when(fineRepository.findByHouseId(houseId)).thenReturn(Collections.emptyList());
        when(bankTransactionRepository.findByHouseReference("B-202")).thenReturn(Collections.emptyList());

        AccountStatementResponse response = useCase.execute(houseId, "OWNER");

        assertNotNull(response);
        assertEquals("B-202", response.houseNumber());
        assertEquals(0, new BigDecimal("350.00").compareTo(response.totalBalance()));
        assertEquals(SolvencyStatus.DELINQUENT, response.solvencyStatus());
    }

    @Test
    @DisplayName("US-204: Compute DELINQUENT status when house has pending fine")
    void shouldReturnDelinquentWhenPendingFineExists() {
        UUID houseId = UUID.randomUUID();
        FineType fineType = new FineType("NOISE", "Ruidos Excesivos", "Música tarde", new BigDecimal("300.00"));
        Fine fine = new Fine(houseId, "C-303", fineType, new BigDecimal("300.00"), "Fiesta ruidosa", null, UUID.randomUUID());

        when(maintenanceChargeRepository.findByHouseId(houseId)).thenReturn(Collections.emptyList());
        when(fineRepository.findByHouseId(houseId)).thenReturn(List.of(fine));
        when(bankTransactionRepository.findByHouseReference("C-303")).thenReturn(Collections.emptyList());

        AccountStatementResponse response = useCase.execute(houseId, "OWNER");

        assertNotNull(response);
        assertEquals("C-303", response.houseNumber());
        assertEquals(0, new BigDecimal("300.00").compareTo(response.totalBalance()));
        assertEquals(0, new BigDecimal("300.00").compareTo(response.pendingFinesBalance()));
        assertEquals(SolvencyStatus.DELINQUENT, response.solvencyStatus());
    }

    @Test
    @DisplayName("US-204 Invariant: Reject access to Tenant users with 403 / TenantStatementAccessException")
    void shouldThrowTenantStatementAccessExceptionWhenUserIsTenant() {
        UUID houseId = UUID.randomUUID();

        assertThrows(TenantStatementAccessException.class, () -> useCase.execute(houseId, "TENANT"));
        assertThrows(TenantStatementAccessException.class, () -> useCase.execute(houseId, "tenant"));

        verify(maintenanceChargeRepository, never()).findByHouseId(any());
        verify(fineRepository, never()).findByHouseId(any());
        verify(bankTransactionRepository, never()).findByHouseReference(any());
    }

    @Test
    @DisplayName("US-204 Invariant: Reject null houseId")
    void shouldThrowExceptionWhenHouseIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, "OWNER"));
    }
}
