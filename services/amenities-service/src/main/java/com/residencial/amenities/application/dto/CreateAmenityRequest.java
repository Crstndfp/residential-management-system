package com.residencial.amenities.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public record CreateAmenityRequest(
        @NotBlank(message = "Amenity code is required")
        @Size(max = 50, message = "Amenity code cannot exceed 50 characters")
        String code,

        @NotBlank(message = "Amenity name is required")
        @Size(max = 100, message = "Amenity name cannot exceed 100 characters")
        String name,

        String description,

        @Min(value = 1, message = "Capacity must be at least 1 person")
        int capacity,

        @NotNull(message = "Rental cost is required")
        @DecimalMin(value = "0.00", message = "Rental cost cannot be negative")
        BigDecimal rentalCost,

        @NotNull(message = "Deposit cost is required")
        @DecimalMin(value = "0.00", message = "Deposit cost cannot be negative")
        BigDecimal depositCost,

        @NotNull(message = "Opening time is required")
        LocalTime openingTime,

        @NotNull(message = "Closing time is required")
        LocalTime closingTime,

        @NotBlank(message = "Allowed event types are required (comma separated)")
        String allowedEventTypes
) {}
