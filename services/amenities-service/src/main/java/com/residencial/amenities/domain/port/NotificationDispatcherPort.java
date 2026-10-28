package com.residencial.amenities.domain.port;

import com.residencial.amenities.domain.model.TicketStatus;

import java.util.UUID;

public interface NotificationDispatcherPort {
    void notifyResidentTicketUpdated(UUID residentUserId, String ticketFolio, TicketStatus newStatus, String notes);
}
