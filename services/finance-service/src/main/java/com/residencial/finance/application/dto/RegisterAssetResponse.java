package com.residencial.finance.application.dto;

import com.residencial.finance.domain.model.AssetCategory;
import com.residencial.finance.domain.model.AssetStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterAssetResponse(
        UUID id,
        String code,
        String name,
        AssetCategory category,
        String physicalLocation,
        BigDecimal acquisitionCost,
        LocalDate acquisitionDate,
        AssetStatus status,
        String notes,
        Instant createdAt
) {}
