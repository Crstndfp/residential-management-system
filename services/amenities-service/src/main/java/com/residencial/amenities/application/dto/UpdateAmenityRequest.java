package com.residencial.amenities.application.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public record UpdateAmenityRequest(
        String name,
        String description,
        Integer capacity,
        BigDecimal rentalCost,
        BigDecimal depositCost,
        LocalTime openingTime,
        LocalTime closingTime,
        String allowedEventTypes
) {}
