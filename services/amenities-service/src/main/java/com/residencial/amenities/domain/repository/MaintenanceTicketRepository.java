package com.residencial.amenities.domain.repository;

import com.residencial.amenities.domain.model.MaintenanceTicket;
import com.residencial.amenities.domain.model.TicketStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceTicketRepository {
    MaintenanceTicket save(MaintenanceTicket ticket);
    Optional<MaintenanceTicket> findById(UUID id);
    Optional<MaintenanceTicket> findByFolio(String folio);
    List<MaintenanceTicket> findAll();
    List<MaintenanceTicket> findByStatus(TicketStatus status);
    List<MaintenanceTicket> findByHouseId(UUID houseId);
    boolean existsByFolio(String folio);
}
