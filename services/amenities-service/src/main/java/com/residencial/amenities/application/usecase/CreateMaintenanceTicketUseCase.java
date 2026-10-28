package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.CreateTicketRequest;
import com.residencial.amenities.application.dto.TicketResponse;
import com.residencial.amenities.domain.model.MaintenanceTicket;
import com.residencial.amenities.domain.model.TicketCategory;
import com.residencial.amenities.domain.model.TicketPriority;
import com.residencial.amenities.domain.model.TicketStatus;
import com.residencial.amenities.domain.model.TicketUpdate;
import com.residencial.amenities.domain.repository.MaintenanceTicketRepository;
import com.residencial.amenities.domain.repository.TicketUpdateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateMaintenanceTicketUseCase {

    private final MaintenanceTicketRepository ticketRepository;
    private final TicketUpdateRepository updateRepository;

    public CreateMaintenanceTicketUseCase(MaintenanceTicketRepository ticketRepository,
                                         TicketUpdateRepository updateRepository) {
        this.ticketRepository = ticketRepository;
        this.updateRepository = updateRepository;
    }

    @Transactional
    public TicketResponse execute(CreateTicketRequest request) {
        TicketCategory category = TicketCategory.valueOf(request.category().toUpperCase().trim());
        TicketPriority priority = (request.priority() != null && !request.priority().isBlank())
                ? TicketPriority.valueOf(request.priority().toUpperCase().trim())
                : TicketPriority.MEDIUM;

        String folio = generateUniqueFolio();

        MaintenanceTicket ticket = new MaintenanceTicket(
                UUID.randomUUID(),
                folio,
                request.reporterUserId(),
                request.reporterName(),
                request.reporterRole(),
                request.houseId(),
                request.houseNumber(),
                request.title(),
                category,
                request.location(),
                request.description(),
                request.photoUrl(),
                TicketStatus.OPEN,
                priority,
                null,
                Instant.now(),
                Instant.now(),
                null
        );

        MaintenanceTicket saved = ticketRepository.save(ticket);

        TicketUpdate initialUpdate = new TicketUpdate(
                UUID.randomUUID(),
                saved.getId(),
                request.reporterUserId(),
                request.reporterName(),
                request.reporterRole(),
                TicketStatus.OPEN,
                TicketStatus.OPEN,
                "Ticket opened with initial status OPEN",
                Instant.now()
        );
        updateRepository.save(initialUpdate);

        return TicketResponse.fromDomain(saved);
    }

    private String generateUniqueFolio() {
        String folio;
        do {
            folio = "TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (ticketRepository.existsByFolio(folio));
        return folio;
    }
}
