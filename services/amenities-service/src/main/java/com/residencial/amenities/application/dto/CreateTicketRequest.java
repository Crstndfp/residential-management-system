package com.residencial.amenities.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateTicketRequest(
        @NotBlank(message = "Title cannot be blank")
        String title,

        @NotBlank(message = "Category cannot be blank")
        String category,

        @NotBlank(message = "Location cannot be blank")
        String location,

        @NotBlank(message = "Description cannot be blank")
        String description,

        String photoUrl,

        String priority,

        @NotNull(message = "Reporter user ID cannot be null")
        UUID reporterUserId,

        @NotBlank(message = "Reporter name cannot be blank")
        String reporterName,

        @NotBlank(message = "Reporter role cannot be blank")
        String reporterRole,

        @NotNull(message = "House ID cannot be null")
        UUID houseId,

        @NotBlank(message = "House number cannot be blank")
        String houseNumber
) {
}
