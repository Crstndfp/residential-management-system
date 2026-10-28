package com.residencial.parking.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterVehicleEntryRequest(
        @NotBlank(message = "License plate cannot be blank")
        String plate,

        @NotNull(message = "House ID cannot be null")
        UUID houseId,

        @NotBlank(message = "House number cannot be blank")
        String houseNumber,

        @NotBlank(message = "Visitor name cannot be blank")
        String visitorName,

        @NotBlank(message = "Visitor ID document cannot be blank")
        String visitorIdDocument,

        @NotNull(message = "Guard user ID cannot be null")
        UUID guardUserId
) {
}
