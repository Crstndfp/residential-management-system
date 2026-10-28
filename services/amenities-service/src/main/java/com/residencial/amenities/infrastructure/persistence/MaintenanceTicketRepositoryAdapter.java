package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.MaintenanceTicket;
import com.residencial.amenities.domain.model.TicketStatus;
import com.residencial.amenities.domain.repository.MaintenanceTicketRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MaintenanceTicketRepositoryAdapter implements MaintenanceTicketRepository {

    private final JpaMaintenanceTicketRepository jpaTicketRepository;

    public MaintenanceTicketRepositoryAdapter(JpaMaintenanceTicketRepository jpaTicketRepository) {
        this.jpaTicketRepository = jpaTicketRepository;
    }

    @Override
    public MaintenanceTicket save(MaintenanceTicket ticket) {
        return jpaTicketRepository.save(ticket);
    }

    @Override
    public Optional<MaintenanceTicket> findById(UUID id) {
        return jpaTicketRepository.findById(id);
    }

    @Override
    public Optional<MaintenanceTicket> findByFolio(String folio) {
        return jpaTicketRepository.findByFolio(folio);
    }

    @Override
    public List<MaintenanceTicket> findAll() {
        return jpaTicketRepository.findAll();
    }

    @Override
    public List<MaintenanceTicket> findByStatus(TicketStatus status) {
        return jpaTicketRepository.findByStatus(status);
    }

    @Override
    public List<MaintenanceTicket> findByHouseId(UUID houseId) {
        return jpaTicketRepository.findByHouseId(houseId);
    }

    @Override
    public boolean existsByFolio(String folio) {
        return jpaTicketRepository.existsByFolio(folio);
    }
}
