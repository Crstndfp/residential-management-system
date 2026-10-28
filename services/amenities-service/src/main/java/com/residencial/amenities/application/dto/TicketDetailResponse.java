package com.residencial.amenities.application.dto;

import java.util.List;

public record TicketDetailResponse(
        TicketResponse ticket,
        List<TicketUpdateResponse> history
) {
}
