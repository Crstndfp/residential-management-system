package com.residencial.finance.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AccountStatementResponse(
        UUID houseId,
        String houseNumber,
        BigDecimal totalBalance,
        BigDecimal pendingMaintenanceBalance,
        BigDecimal pendingFinesBalance,
        BigDecimal totalPaid,
        SolvencyStatus solvencyStatus,
        List<StatementMovementDto> movements
) {}
