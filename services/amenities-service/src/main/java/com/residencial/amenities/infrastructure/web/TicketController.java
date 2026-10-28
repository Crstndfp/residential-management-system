package com.residencial.amenities.infrastructure.web;

import com.residencial.amenities.application.dto.*;
import com.residencial.amenities.application.usecase.CreateMaintenanceTicketUseCase;
import com.residencial.amenities.application.usecase.GetTicketDetailUseCase;
import com.residencial.amenities.application.usecase.ListTicketsUseCase;
import com.residencial.amenities.application.usecase.UpdateTicketProgressUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/amenities/tickets")
public class TicketController {

    private final CreateMaintenanceTicketUseCase createTicketUseCase;
    private final UpdateTicketProgressUseCase updateTicketProgressUseCase;
    private final GetTicketDetailUseCase getTicketDetailUseCase;
    private final ListTicketsUseCase listTicketsUseCase;

    public TicketController(CreateMaintenanceTicketUseCase createTicketUseCase,
                            UpdateTicketProgressUseCase updateTicketProgressUseCase,
                            GetTicketDetailUseCase getTicketDetailUseCase,
                            ListTicketsUseCase listTicketsUseCase) {
        this.createTicketUseCase = createTicketUseCase;
        this.updateTicketProgressUseCase = updateTicketProgressUseCase;
        this.getTicketDetailUseCase = getTicketDetailUseCase;
        this.listTicketsUseCase = listTicketsUseCase;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = createTicketUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> listTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID houseId) {
        List<TicketResponse> tickets = listTicketsUseCase.execute(status, houseId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailResponse> getTicketDetail(@PathVariable UUID id) {
        TicketDetailResponse response = getTicketDetailUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<TicketDetailResponse> updateProgress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketProgressRequest request) {
        TicketDetailResponse response = updateTicketProgressUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }
}
