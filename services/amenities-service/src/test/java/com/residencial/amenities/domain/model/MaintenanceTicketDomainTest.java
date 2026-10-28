package com.residencial.amenities.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaintenanceTicketDomainTest {

    @Test
    @DisplayName("Successfully creates a MaintenanceTicket with OPEN status")
    void createTicketSuccess() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();

        MaintenanceTicket ticket = new MaintenanceTicket(
                id,
                "TCK-12345678",
                userId,
                "Jane Doe",
                "OWNER",
                houseId,
                "C-302",
                "Broken Lamp in Main St.",
                TicketCategory.LIGHTING,
                "Main Street opposite Lot 15",
                "Light bulb is broken and flickers",
                "https://storage.local/photos/lamp.jpg",
                TicketStatus.OPEN,
                TicketPriority.HIGH,
                null,
                Instant.now(),
                Instant.now(),
                null
        );

        assertThat(ticket.getId()).isEqualTo(id);
        assertThat(ticket.getFolio()).isEqualTo("TCK-12345678");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(ticket.getCategory()).isEqualTo(TicketCategory.LIGHTING);
        assertThat(ticket.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(ticket.getAssignedTechnician()).isNull();
        assertThat(ticket.getClosedAt()).isNull();
    }

    @Test
    @DisplayName("Updates ticket status through valid transitions")
    void statusTransitionSuccess() {
        MaintenanceTicket ticket = new MaintenanceTicket(
                UUID.randomUUID(),
                "TCK-87654321",
                UUID.randomUUID(),
                "Carlos",
                "TENANT",
                UUID.randomUUID(),
                "A-101",
                "Pool filter clogged",
                TicketCategory.POOL,
                "Main Pool pump room",
                "Pump making unusual noise",
                null,
                TicketStatus.OPEN,
                TicketPriority.MEDIUM,
                null,
                Instant.now(),
                Instant.now(),
                null
        );

        ticket.updateStatus(TicketStatus.IN_PROGRESS, "Tech John");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(ticket.getAssignedTechnician()).isEqualTo("Tech John");

        ticket.updateStatus(TicketStatus.RESOLVED, null);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
        assertThat(ticket.getClosedAt()).isNotNull();

        ticket.updateStatus(TicketStatus.CLOSED, null);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CLOSED);
    }

    @Test
    @DisplayName("Throws IllegalStateException when attempting to update status of a CLOSED ticket")
    void cannotUpdateClosedTicket() {
        MaintenanceTicket ticket = new MaintenanceTicket(
                UUID.randomUUID(),
                "TCK-99999999",
                UUID.randomUUID(),
                "Carlos",
                "OWNER",
                UUID.randomUUID(),
                "A-101",
                "Resolved Issue",
                TicketCategory.GARDENING,
                "Park",
                "Trim hedges",
                null,
                TicketStatus.CLOSED,
                TicketPriority.LOW,
                "Tech Mark",
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(1800),
                Instant.now().minusSeconds(1800)
        );

        assertThatThrownBy(() -> ticket.updateStatus(TicketStatus.IN_PROGRESS, "Tech Dave"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot change status of an already closed ticket");
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when required fields are missing")
    void validationErrors() {
        assertThatThrownBy(() -> new MaintenanceTicket(
                UUID.randomUUID(),
                "",
                UUID.randomUUID(),
                "Carlos",
                "OWNER",
                UUID.randomUUID(),
                "A-101",
                "Title",
                TicketCategory.OTHER,
                "Location",
                "Desc",
                null,
                TicketStatus.OPEN,
                TicketPriority.LOW,
                null,
                Instant.now(),
                Instant.now(),
                null
        )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("folio cannot be blank");
    }
}
