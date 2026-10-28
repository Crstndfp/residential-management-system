package com.residencial.amenities.domain.repository;

import com.residencial.amenities.domain.model.TicketUpdate;

import java.util.List;
import java.util.UUID;

public interface TicketUpdateRepository {
    TicketUpdate save(TicketUpdate update);
    List<TicketUpdate> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
