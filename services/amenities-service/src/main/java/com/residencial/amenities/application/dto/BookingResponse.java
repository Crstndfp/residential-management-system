package com.residencial.amenities.application.dto;

import com.residencial.amenities.domain.model.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID amenityId,
        String amenityName,
        UUID houseId,
        String houseNumber,
        UUID residentUserId,
        String residentName,
        String eventType,
        LocalDate bookingDate,
        LocalTime startTime,
        LocalTime endTime,
        BookingStatus status,
        BigDecimal totalCost,
        BigDecimal depositAmount,
        UUID financeChargeId,
        Instant createdAt
) {}
