package com.residencial.amenities.application.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull(message = "Amenity ID is required")
        UUID amenityId,

        @NotNull(message = "House ID is required")
        UUID houseId,

        @NotBlank(message = "House number is required")
        @Size(max = 20, message = "House number cannot exceed 20 characters")
        String houseNumber,

        @NotNull(message = "Resident user ID is required")
        UUID residentUserId,

        @NotBlank(message = "Resident name is required")
        @Size(max = 150, message = "Resident name cannot exceed 150 characters")
        String residentName,

        @NotBlank(message = "Event type is required")
        String eventType,

        @NotNull(message = "Booking date is required")
        @FutureOrPresent(message = "Booking date must be today or in the future")
        LocalDate bookingDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime
) {}
