package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.TicketDetailResponse;
import com.residencial.amenities.application.dto.TicketResponse;
import com.residencial.amenities.application.dto.TicketUpdateResponse;
import com.residencial.amenities.domain.exception.TicketNotFoundException;
import com.residencial.amenities.domain.model.MaintenanceTicket;
import com.residencial.amenities.domain.repository.MaintenanceTicketRepository;
import com.residencial.amenities.domain.repository.TicketUpdateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetTicketDetailUseCase {

    private final MaintenanceTicketRepository ticketRepository;
    private final TicketUpdateRepository updateRepository;

    public GetTicketDetailUseCase(MaintenanceTicketRepository ticketRepository,
                                  TicketUpdateRepository updateRepository) {
        this.ticketRepository = ticketRepository;
        this.updateRepository = updateRepository;
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse execute(UUID ticketId) {
        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        List<TicketUpdateResponse> history = updateRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(TicketUpdateResponse::fromDomain)
                .toList();

        return new TicketDetailResponse(TicketResponse.fromDomain(ticket), history);
    }
}
