package com.residencial.amenities.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ConvokeAssemblyRequest(
        @NotBlank(message = "Title cannot be blank")
        String title,

        @NotBlank(message = "Assembly type cannot be blank")
        String assemblyType,

        @NotNull(message = "Scheduled at cannot be null")
        Instant scheduledAt,

        @NotBlank(message = "Agenda cannot be blank")
        String agenda,

        Integer totalResidentialHouses
) {
}
