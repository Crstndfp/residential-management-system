package com.residencial.amenities.domain.exception;

import java.util.UUID;

public class TicketNotFoundException extends AmenitiesDomainException {
    public TicketNotFoundException(UUID id) {
        super("TICKET_NOT_FOUND", "Maintenance ticket not found with ID: " + id);
    }

    public TicketNotFoundException(String folio) {
        super("TICKET_NOT_FOUND", "Maintenance ticket not found with folio: " + folio);
    }
}
