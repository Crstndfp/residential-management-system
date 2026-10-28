package com.residencial.finance.application.dto;

import com.residencial.finance.domain.model.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateAssetStatusRequest(
        @NotNull(message = "New status is required")
        AssetStatus newStatus,

        @NotBlank(message = "Reason for status change is required")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason,

        UUID changedByUserId
) {}
