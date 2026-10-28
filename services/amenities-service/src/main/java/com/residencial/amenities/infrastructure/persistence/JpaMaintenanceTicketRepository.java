package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.MaintenanceTicket;
import com.residencial.amenities.domain.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaMaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, UUID> {
    Optional<MaintenanceTicket> findByFolio(String folio);
    List<MaintenanceTicket> findByStatus(TicketStatus status);
    List<MaintenanceTicket> findByHouseId(UUID houseId);
    boolean existsByFolio(String folio);
}
