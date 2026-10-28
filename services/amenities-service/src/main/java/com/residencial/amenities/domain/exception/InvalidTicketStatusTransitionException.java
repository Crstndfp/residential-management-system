package com.residencial.amenities.domain.exception;

import com.residencial.amenities.domain.model.TicketStatus;

public class InvalidTicketStatusTransitionException extends AmenitiesDomainException {
    public InvalidTicketStatusTransitionException(TicketStatus currentStatus, TicketStatus newStatus) {
        super("INVALID_TICKET_STATUS_TRANSITION",
                "Cannot transition ticket from status " + currentStatus + " to " + newStatus);
    }
}
