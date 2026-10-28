package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.TicketUpdate;
import com.residencial.amenities.domain.repository.TicketUpdateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TicketUpdateRepositoryAdapter implements TicketUpdateRepository {

    private final JpaTicketUpdateRepository jpaTicketUpdateRepository;

    public TicketUpdateRepositoryAdapter(JpaTicketUpdateRepository jpaTicketUpdateRepository) {
        this.jpaTicketUpdateRepository = jpaTicketUpdateRepository;
    }

    @Override
    public TicketUpdate save(TicketUpdate update) {
        return jpaTicketUpdateRepository.save(update);
    }

    @Override
    public List<TicketUpdate> findByTicketIdOrderByCreatedAtAsc(UUID ticketId) {
        return jpaTicketUpdateRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }
}
