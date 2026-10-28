package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.TicketDetailResponse;
import com.residencial.amenities.application.dto.UpdateTicketProgressRequest;
import com.residencial.amenities.domain.exception.InvalidTicketStatusTransitionException;
import com.residencial.amenities.domain.exception.TicketNotFoundException;
import com.residencial.amenities.domain.model.*;
import com.residencial.amenities.domain.port.NotificationDispatcherPort;
import com.residencial.amenities.domain.repository.MaintenanceTicketRepository;
import com.residencial.amenities.domain.repository.TicketUpdateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateTicketProgressUseCaseTest {

    private MaintenanceTicketRepository ticketRepository;
    private TicketUpdateRepository updateRepository;
    private NotificationDispatcherPort notificationDispatcher;
    private UpdateTicketProgressUseCase updateTicketProgressUseCase;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(MaintenanceTicketRepository.class);
        updateRepository = mock(TicketUpdateRepository.class);
        notificationDispatcher = mock(NotificationDispatcherPort.class);
        updateTicketProgressUseCase = new UpdateTicketProgressUseCase(
                ticketRepository,
                updateRepository,
                notificationDispatcher
        );
    }

    @Test
    @DisplayName("Successfully updates ticket to IN_PROGRESS, assigns technician and dispatches notification")
    void updateTicketProgressSuccess() {
        UUID ticketId = UUID.randomUUID();
        UUID residentUserId = UUID.randomUUID();

        MaintenanceTicket ticket = new MaintenanceTicket(
                ticketId,
                "TCK-11223344",
                residentUserId,
                "Carlos",
                "OWNER",
                UUID.randomUUID(),
                "A-102",
                "Pothole in entrance",
                TicketCategory.OTHER,
                "Main gate",
                "Pothole needs asphalt patching",
                null,
                TicketStatus.OPEN,
                TicketPriority.HIGH,
                null,
                Instant.now(),
                Instant.now(),
                null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(MaintenanceTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketUpdate auditUpdate = new TicketUpdate(
                UUID.randomUUID(),
                ticketId,
                UUID.randomUUID(),
                "Admin Mike",
                "ADMIN",
                TicketStatus.OPEN,
                TicketStatus.IN_PROGRESS,
                "Assigned to external paving team",
                Instant.now()
        );
        when(updateRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)).thenReturn(List.of(auditUpdate));

        UpdateTicketProgressRequest request = new UpdateTicketProgressRequest(
                UUID.randomUUID(),
                "Admin Mike",
                "ADMIN",
                "IN_PROGRESS",
                "Paving Team Alpha",
                "Assigned to external paving team"
        );

        TicketDetailResponse response = updateTicketProgressUseCase.execute(ticketId, request);

        assertThat(response).isNotNull();
        assertThat(response.ticket().status()).isEqualTo("IN_PROGRESS");
        assertThat(response.ticket().assignedTechnician()).isEqualTo("Paving Team Alpha");
        assertThat(response.history()).hasSize(1);

        verify(notificationDispatcher).notifyResidentTicketUpdated(
                eq(residentUserId),
                eq("TCK-11223344"),
                eq(TicketStatus.IN_PROGRESS),
                eq("Assigned to external paving team")
        );
    }

    @Test
    @DisplayName("Throws TicketNotFoundException when updating non-existent ticket")
    void ticketNotFoundThrows() {
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        UpdateTicketProgressRequest request = new UpdateTicketProgressRequest(
                UUID.randomUUID(),
                "Admin",
                "ADMIN",
                "IN_PROGRESS",
                "Tech",
                "Notes"
        );

        assertThatThrownBy(() -> updateTicketProgressUseCase.execute(ticketId, request))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    @DisplayName("Throws InvalidTicketStatusTransitionException when attempting to modify a CLOSED ticket")
    void closedTicketThrows() {
        UUID ticketId = UUID.randomUUID();
        MaintenanceTicket ticket = new MaintenanceTicket(
                ticketId,
                "TCK-55667788",
                UUID.randomUUID(),
                "Carlos",
                "OWNER",
                UUID.randomUUID(),
                "A-102",
                "Cleaned pool",
                TicketCategory.POOL,
                "Clubhouse",
                "Routine cleaning",
                null,
                TicketStatus.CLOSED,
                TicketPriority.LOW,
                "Tech Dave",
                Instant.now().minusSeconds(7200),
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(3600)
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        UpdateTicketProgressRequest request = new UpdateTicketProgressRequest(
                UUID.randomUUID(),
                "Admin",
                "ADMIN",
                "IN_PROGRESS",
                "Tech",
                "Reopen attempt"
        );

        assertThatThrownBy(() -> updateTicketProgressUseCase.execute(ticketId, request))
                .isInstanceOf(InvalidTicketStatusTransitionException.class);
    }
}
