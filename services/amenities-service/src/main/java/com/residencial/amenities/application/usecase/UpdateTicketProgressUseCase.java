package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.TicketDetailResponse;
import com.residencial.amenities.application.dto.TicketResponse;
import com.residencial.amenities.application.dto.TicketUpdateResponse;
import com.residencial.amenities.application.dto.UpdateTicketProgressRequest;
import com.residencial.amenities.domain.exception.InvalidTicketStatusTransitionException;
import com.residencial.amenities.domain.exception.TicketNotFoundException;
import com.residencial.amenities.domain.model.MaintenanceTicket;
import com.residencial.amenities.domain.model.TicketStatus;
import com.residencial.amenities.domain.model.TicketUpdate;
import com.residencial.amenities.domain.port.NotificationDispatcherPort;
import com.residencial.amenities.domain.repository.MaintenanceTicketRepository;
import com.residencial.amenities.domain.repository.TicketUpdateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UpdateTicketProgressUseCase {

    private final MaintenanceTicketRepository ticketRepository;
    private final TicketUpdateRepository updateRepository;
    private final NotificationDispatcherPort notificationDispatcher;

    public UpdateTicketProgressUseCase(MaintenanceTicketRepository ticketRepository,
                                       TicketUpdateRepository updateRepository,
                                       NotificationDispatcherPort notificationDispatcher) {
        this.ticketRepository = ticketRepository;
        this.updateRepository = updateRepository;
        this.notificationDispatcher = notificationDispatcher;
    }

    @Transactional
    public TicketDetailResponse execute(UUID ticketId, UpdateTicketProgressRequest request) {
        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        TicketStatus newStatus = TicketStatus.valueOf(request.newStatus().toUpperCase().trim());
        TicketStatus previousStatus = ticket.getStatus();

        validateTransition(previousStatus, newStatus);

        ticket.updateStatus(newStatus, request.assignedTechnician());
        MaintenanceTicket saved = ticketRepository.save(ticket);

        TicketUpdate update = new TicketUpdate(
                UUID.randomUUID(),
                ticketId,
                request.authorUserId(),
                request.authorName(),
                request.authorRole(),
                previousStatus,
                newStatus,
                request.notes(),
                Instant.now()
        );
        updateRepository.save(update);

        // Dispatch push notification to resident
        notificationDispatcher.notifyResidentTicketUpdated(
                ticket.getReporterUserId(),
                ticket.getFolio(),
                newStatus,
                request.notes()
        );

        List<TicketUpdateResponse> history = updateRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(TicketUpdateResponse::fromDomain)
                .toList();

        return new TicketDetailResponse(TicketResponse.fromDomain(saved), history);
    }

    private void validateTransition(TicketStatus current, TicketStatus next) {
        if (current == TicketStatus.CLOSED) {
            throw new InvalidTicketStatusTransitionException(current, next);
        }
        if (current == TicketStatus.RESOLVED && next == TicketStatus.IN_PROGRESS) {
            throw new InvalidTicketStatusTransitionException(current, next);
        }
        if (current == TicketStatus.IN_PROGRESS && next == TicketStatus.OPEN) {
            throw new InvalidTicketStatusTransitionException(current, next);
        }
    }
}
