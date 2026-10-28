package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.AccountStatementResponse;
import com.residencial.finance.application.dto.MovementType;
import com.residencial.finance.application.dto.SolvencyStatus;
import com.residencial.finance.application.dto.StatementMovementDto;
import com.residencial.finance.domain.exception.TenantStatementAccessException;
import com.residencial.finance.domain.model.BankTransaction;
import com.residencial.finance.domain.model.BankTransactionStatus;
import com.residencial.finance.domain.model.ChargeStatus;
import com.residencial.finance.domain.model.Fine;
import com.residencial.finance.domain.model.FineStatus;
import com.residencial.finance.domain.model.MaintenanceCharge;
import com.residencial.finance.domain.repository.BankTransactionRepository;
import com.residencial.finance.domain.repository.FineRepository;
import com.residencial.finance.domain.repository.MaintenanceChargeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class GetAccountStatementUseCase {

    private final MaintenanceChargeRepository maintenanceChargeRepository;
    private final FineRepository fineRepository;
    private final BankTransactionRepository bankTransactionRepository;

    public GetAccountStatementUseCase(MaintenanceChargeRepository maintenanceChargeRepository,
                                      FineRepository fineRepository,
                                      BankTransactionRepository bankTransactionRepository) {
        this.maintenanceChargeRepository = maintenanceChargeRepository;
        this.fineRepository = fineRepository;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    @Transactional(readOnly = true)
    public AccountStatementResponse execute(UUID houseId, String userRole) {
        if (houseId == null) {
            throw new IllegalArgumentException("House ID cannot be null.");
        }

        // US-204 Acceptance Criteria: Tenant users are blocked from viewing account statements
        if (userRole != null && "TENANT".equalsIgnoreCase(userRole.trim())) {
            throw new TenantStatementAccessException();
        }

        List<MaintenanceCharge> charges = maintenanceChargeRepository.findByHouseId(houseId);
        List<Fine> fines = fineRepository.findByHouseId(houseId);

        String houseNumber = "N/A";
        if (!charges.isEmpty()) {
            houseNumber = charges.getFirst().getHouseNumber();
        } else if (!fines.isEmpty()) {
            houseNumber = fines.getFirst().getHouseNumber();
        }

        List<BankTransaction> transactions = "N/A".equals(houseNumber)
                ? Collections.emptyList()
                : bankTransactionRepository.findByHouseReference(houseNumber);

        BigDecimal pendingMaintenanceBalance = BigDecimal.ZERO;
        boolean hasOverdueCharge = false;
        LocalDate today = LocalDate.now();

        for (MaintenanceCharge charge : charges) {
            if (charge.getStatus() != ChargeStatus.PAID) {
                BigDecimal pending = charge.getAmount().subtract(charge.getPaidAmount());
                pendingMaintenanceBalance = pendingMaintenanceBalance.add(pending);
                if (charge.getDueDate().isBefore(today)) {
                    hasOverdueCharge = true;
                }
            }
        }

        BigDecimal pendingFinesBalance = BigDecimal.ZERO;
        boolean hasPendingFine = false;

        for (Fine fine : fines) {
            if (fine.getStatus() == FineStatus.PENDING) {
                pendingFinesBalance = pendingFinesBalance.add(fine.getAmount());
                hasPendingFine = true;
            }
        }

        BigDecimal totalPaid = BigDecimal.ZERO;
        for (BankTransaction tx : transactions) {
            if (tx.getStatus() == BankTransactionStatus.RECONCILED) {
                totalPaid = totalPaid.add(tx.getAmount());
            }
        }

        BigDecimal totalBalance = pendingMaintenanceBalance.add(pendingFinesBalance);

        SolvencyStatus solvencyStatus = (totalBalance.compareTo(BigDecimal.ZERO) > 0 && (hasOverdueCharge || hasPendingFine))
                ? SolvencyStatus.DELINQUENT
                : SolvencyStatus.SOLVENT;

        List<StatementMovementDto> movements = new ArrayList<>();

        for (MaintenanceCharge charge : charges) {
            LocalDate moveDate = charge.getCreatedAt() != null
                    ? LocalDate.ofInstant(charge.getCreatedAt(), ZoneId.systemDefault())
                    : charge.getDueDate();

            movements.add(new StatementMovementDto(
                    charge.getId(),
                    MovementType.MAINTENANCE_CHARGE,
                    "Cuota de Mantenimiento " + charge.getPeriodMonth() + "/" + charge.getPeriodYear(),
                    "CUOTA-" + charge.getPeriodYear() + "-" + charge.getPeriodMonth(),
                    charge.getAmount(),
                    moveDate,
                    charge.getDueDate(),
                    charge.getStatus().name()
            ));
        }

        for (Fine fine : fines) {
            LocalDate moveDate = fine.getCreatedAt() != null
                    ? LocalDate.ofInstant(fine.getCreatedAt(), ZoneId.systemDefault())
                    : today;

            movements.add(new StatementMovementDto(
                    fine.getId(),
                    MovementType.FINE,
                    "Multa: " + fine.getFineType().getName(),
                    fine.getFineType().getCode(),
                    fine.getAmount(),
                    moveDate,
                    null,
                    fine.getStatus().name()
            ));
        }

        for (BankTransaction tx : transactions) {
            if (tx.getStatus() == BankTransactionStatus.RECONCILED) {
                movements.add(new StatementMovementDto(
                        tx.getId(),
                        MovementType.PAYMENT,
                        "Pago Bancario Conciliado (Aut: " + tx.getAuthorizationNumber() + ")",
                        tx.getAuthorizationNumber(),
                        tx.getAmount(),
                        tx.getTransactionDate(),
                        null,
                        tx.getStatus().name()
                ));
            }
        }

        movements.sort((m1, m2) -> {
            int dateComp = m2.date().compareTo(m1.date());
            if (dateComp != 0) {
                return dateComp;
            }
            return m1.movementType().compareTo(m2.movementType());
        });

        return new AccountStatementResponse(
                houseId,
                houseNumber,
                totalBalance,
                pendingMaintenanceBalance,
                pendingFinesBalance,
                totalPaid,
                solvencyStatus,
                movements
        );
    }
}
