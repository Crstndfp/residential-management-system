package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.TicketResponse;
import com.residencial.amenities.domain.model.MaintenanceTicket;
import com.residencial.amenities.domain.model.TicketStatus;
import com.residencial.amenities.domain.repository.MaintenanceTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListTicketsUseCase {

    private final MaintenanceTicketRepository ticketRepository;

    public ListTicketsUseCase(MaintenanceTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> execute(String status, UUID houseId) {
        List<MaintenanceTicket> tickets;

        if (status != null && !status.isBlank()) {
            TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase().trim());
            tickets = ticketRepository.findByStatus(ticketStatus);
        } else if (houseId != null) {
            tickets = ticketRepository.findByHouseId(houseId);
        } else {
            tickets = ticketRepository.findAll();
        }

        return tickets.stream()
                .map(TicketResponse::fromDomain)
                .toList();
    }
}
