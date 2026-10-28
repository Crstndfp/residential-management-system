package com.residencial.finance.application.dto;

import com.residencial.finance.domain.model.FineStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ApplyFineResponse(
        UUID fineId,
        UUID houseId,
        String houseNumber,
        String fineTypeCode,
        String fineTypeName,
        BigDecimal amount,
        String reason,
        String evidenceUrl,
        FineStatus status,
        Instant createdAt
) {}
