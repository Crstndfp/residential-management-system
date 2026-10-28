package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.TicketUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaTicketUpdateRepository extends JpaRepository<TicketUpdate, UUID> {
    List<TicketUpdate> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
