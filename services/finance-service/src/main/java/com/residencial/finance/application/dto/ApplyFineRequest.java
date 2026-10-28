package com.residencial.finance.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplyFineRequest(
        @NotNull(message = "House ID is required")
        UUID houseId,

        @NotBlank(message = "House number is required")
        @Size(max = 20, message = "House number cannot exceed 20 characters")
        String houseNumber,

        @NotBlank(message = "Fine type code is required")
        @Size(max = 50, message = "Fine type code cannot exceed 50 characters")
        String fineTypeCode,

        @DecimalMin(value = "0.01", message = "Custom amount must be greater than zero if specified")
        BigDecimal customAmount,

        @NotBlank(message = "Infraction reason is required")
        String reason,

        @Size(max = 255, message = "Evidence URL cannot exceed 255 characters")
        String evidenceUrl,

        UUID appliedByUserId
) {}
