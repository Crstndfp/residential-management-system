package com.residencial.finance.application.dto;

import com.residencial.finance.domain.model.AssetCategory;
import com.residencial.finance.domain.model.AssetStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterAssetRequest(
        @NotBlank(message = "Asset code is required")
        @Size(max = 50, message = "Asset code cannot exceed 50 characters")
        String code,

        @NotBlank(message = "Asset name is required")
        @Size(max = 150, message = "Asset name cannot exceed 150 characters")
        String name,

        @NotNull(message = "Asset category is required")
        AssetCategory category,

        @NotBlank(message = "Physical location is required")
        @Size(max = 150, message = "Physical location cannot exceed 150 characters")
        String physicalLocation,

        @NotNull(message = "Acquisition cost is required")
        @DecimalMin(value = "0.01", message = "Acquisition cost must be strictly greater than zero")
        BigDecimal acquisitionCost,

        @NotNull(message = "Acquisition date is required")
        LocalDate acquisitionDate,

        AssetStatus status,

        String notes
) {}
