package com.residencial.amenities.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record AmenityResponse(
        UUID id,
        String code,
        String name,
        String description,
        int capacity,
        BigDecimal rentalCost,
        BigDecimal depositCost,
        LocalTime openingTime,
        LocalTime closingTime,
        String allowedEventTypes,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {}
