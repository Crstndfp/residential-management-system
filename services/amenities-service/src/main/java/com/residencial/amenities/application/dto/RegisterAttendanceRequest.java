package com.residencial.amenities.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterAttendanceRequest(
        @NotNull(message = "House ID cannot be null")
        UUID houseId,

        @NotBlank(message = "House number cannot be blank")
        String houseNumber,

        @NotNull(message = "Attended by user ID cannot be null")
        UUID attendedByUserId,

        @NotBlank(message = "Attended by name cannot be blank")
        String attendedByName,

        Boolean isPresent
) {
}
