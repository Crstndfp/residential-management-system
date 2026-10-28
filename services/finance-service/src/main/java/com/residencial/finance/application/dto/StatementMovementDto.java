package com.residencial.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StatementMovementDto(
        UUID id,
        MovementType movementType,
        String concept,
        String reference,
        BigDecimal amount,
        LocalDate date,
        LocalDate dueDate,
        String status
) {}
