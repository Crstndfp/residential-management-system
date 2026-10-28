package com.residencial.amenities.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMotionRequest(
        @NotBlank(message = "Title cannot be blank")
        String title,

        @NotBlank(message = "Description cannot be blank")
        String description
) {
}
