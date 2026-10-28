package com.residencial.amenities.application.dto;

import com.residencial.amenities.domain.model.TicketUpdate;

import java.time.Instant;
import java.util.UUID;

public record TicketUpdateResponse(
        UUID id,
        UUID ticketId,
        UUID authorUserId,
        String authorName,
        String authorRole,
        String previousStatus,
        String newStatus,
        String notes,
        Instant createdAt
) {
    public static TicketUpdateResponse fromDomain(TicketUpdate update) {
        return new TicketUpdateResponse(
                update.getId(),
                update.getTicketId(),
                update.getAuthorUserId(),
                update.getAuthorName(),
                update.getAuthorRole(),
                update.getPreviousStatus().name(),
                update.getNewStatus().name(),
                update.getNotes(),
                update.getCreatedAt()
        );
    }
}
