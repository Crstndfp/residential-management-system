package com.residencial.parking.infrastructure.web;

import com.residencial.parking.application.dto.*;
import com.residencial.parking.application.usecase.ListParkingSpotsUseCase;
import com.residencial.parking.application.usecase.RegisterVehicleEntryUseCase;
import com.residencial.parking.application.usecase.RegisterVehicleExitUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parking")
public class GateController {

    private final RegisterVehicleEntryUseCase registerEntryUseCase;
    private final RegisterVehicleExitUseCase registerExitUseCase;
    private final ListParkingSpotsUseCase listParkingSpotsUseCase;

    public GateController(RegisterVehicleEntryUseCase registerEntryUseCase,
                          RegisterVehicleExitUseCase registerExitUseCase,
                          ListParkingSpotsUseCase listParkingSpotsUseCase) {
        this.registerEntryUseCase = registerEntryUseCase;
        this.registerExitUseCase = registerExitUseCase;
        this.listParkingSpotsUseCase = listParkingSpotsUseCase;
    }

    @PostMapping("/entries")
    public ResponseEntity<VehicleEntryResponse> registerEntry(@Valid @RequestBody RegisterVehicleEntryRequest request) {
        VehicleEntryResponse response = registerEntryUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/entries/{id}/exit")
    public ResponseEntity<VehicleExitResponse> registerExitById(
            @PathVariable UUID id,
            @RequestBody(required = false) RegisterVehicleExitRequest request) {
        VehicleExitResponse response = registerExitUseCase.executeById(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/entries/exit-by-plate")
    public ResponseEntity<VehicleExitResponse> registerExitByPlate(
            @RequestParam String plate,
            @RequestBody(required = false) RegisterVehicleExitRequest request) {
        VehicleExitResponse response = registerExitUseCase.executeByPlate(plate, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/spots")
    public ResponseEntity<List<ParkingSpotStatusResponse>> listSpots() {
        List<ParkingSpotStatusResponse> spots = listParkingSpotsUseCase.execute();
        return ResponseEntity.ok(spots);
    }
}
