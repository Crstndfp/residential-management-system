package com.residencial.amenities.application.dto;

import com.residencial.amenities.domain.model.MaintenanceTicket;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String folio,
        String title,
        String category,
        String location,
        String description,
        String photoUrl,
        String status,
        String priority,
        String assignedTechnician,
        UUID reporterUserId,
        String reporterName,
        String reporterRole,
        UUID houseId,
        String houseNumber,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt
) {
    public static TicketResponse fromDomain(MaintenanceTicket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getFolio(),
                ticket.getTitle(),
                ticket.getCategory().name(),
                ticket.getLocation(),
                ticket.getDescription(),
                ticket.getPhotoUrl(),
                ticket.getStatus().name(),
                ticket.getPriority().name(),
                ticket.getAssignedTechnician(),
                ticket.getReporterUserId(),
                ticket.getReporterName(),
                ticket.getReporterRole(),
                ticket.getHouseId(),
                ticket.getHouseNumber(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt()
        );
    }
}
