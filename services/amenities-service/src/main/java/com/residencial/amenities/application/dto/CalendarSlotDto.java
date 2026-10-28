package com.residencial.amenities.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record CalendarSlotDto(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String displayStatus
) {}
