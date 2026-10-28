package com.residencial.finance.application.dto;

import com.residencial.finance.domain.model.AssetStatus;

import java.time.Instant;
import java.util.UUID;

public record AssetStatusHistoryDto(
        UUID id,
        UUID assetId,
        AssetStatus previousStatus,
        AssetStatus newStatus,
        String reason,
        UUID changedByUserId,
        Instant createdAt
) {}
