package com.residencial.parking.application.dto;

import java.time.Instant;

public record RegisterVehicleExitRequest(
        Instant exitTime
) {
}
