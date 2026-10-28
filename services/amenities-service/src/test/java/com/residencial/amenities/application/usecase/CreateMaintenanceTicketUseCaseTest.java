package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.CreateTicketRequest;
import com.residencial.amenities.application.dto.TicketResponse;
import com.residencial.amenities.domain.model.MaintenanceTicket;
import com.residencial.amenities.domain.model.TicketUpdate;
import com.residencial.amenities.domain.repository.MaintenanceTicketRepository;
import com.residencial.amenities.domain.repository.TicketUpdateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateMaintenanceTicketUseCaseTest {

    private MaintenanceTicketRepository ticketRepository;
    private TicketUpdateRepository updateRepository;
    private CreateMaintenanceTicketUseCase createTicketUseCase;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(MaintenanceTicketRepository.class);
        updateRepository = mock(TicketUpdateRepository.class);
        createTicketUseCase = new CreateMaintenanceTicketUseCase(ticketRepository, updateRepository);
    }

    @Test
    @DisplayName("Successfully creates ticket with unique folio, OPEN status and initial update")
    void createTicketSuccess() {
        UUID reporterUserId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();

        CreateTicketRequest request = new CreateTicketRequest(
                "Gym Treadmill Malfunction",
                "SECURITY_EQUIPMENT",
                "Clubhouse Gym - 2nd floor",
                "Belt is slipping when running above 8km/h",
                "https://storage.local/photos/gym1.jpg",
                "HIGH",
                reporterUserId,
                "Robert Miller",
                "OWNER",
                houseId,
                "D-401"
        );

        when(ticketRepository.existsByFolio(anyString())).thenReturn(false);
        when(ticketRepository.save(any(MaintenanceTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateRepository.save(any(TicketUpdate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = createTicketUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.folio()).startsWith("TCK-");
        assertThat(response.title()).isEqualTo("Gym Treadmill Malfunction");
        assertThat(response.category()).isEqualTo("SECURITY_EQUIPMENT");
        assertThat(response.status()).isEqualTo("OPEN");
        assertThat(response.priority()).isEqualTo("HIGH");
        assertThat(response.houseNumber()).isEqualTo("D-401");

        ArgumentCaptor<TicketUpdate> updateCaptor = ArgumentCaptor.forClass(TicketUpdate.class);
        verify(updateRepository).save(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getPreviousStatus().name()).isEqualTo("OPEN");
        assertThat(updateCaptor.getValue().getNewStatus().name()).isEqualTo("OPEN");
    }
}
