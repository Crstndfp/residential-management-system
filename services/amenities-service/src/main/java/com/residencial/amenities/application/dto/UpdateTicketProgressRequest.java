package com.residencial.amenities.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateTicketProgressRequest(
        @NotNull(message = "Author user ID cannot be null")
        UUID authorUserId,

        @NotBlank(message = "Author name cannot be blank")
        String authorName,

        @NotBlank(message = "Author role cannot be blank")
        String authorRole,

        @NotBlank(message = "New status cannot be blank")
        String newStatus,

        String assignedTechnician,

        @NotBlank(message = "Notes cannot be blank")
        String notes
) {
}
