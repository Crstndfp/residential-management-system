package com.residencial.parking.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ApplyParkingFineRequest(
        @NotBlank(message = "License plate cannot be blank")
        String plate,

        @NotNull(message = "House ID cannot be null")
        UUID houseId,

        @NotBlank(message = "House number cannot be blank")
        String houseNumber,

        BigDecimal fineAmount,

        String reason,

        @NotNull(message = "Applying user ID cannot be null")
        UUID appliedByUserId,

        @NotBlank(message = "Applying user name cannot be blank")
        String appliedByName
) {
}
