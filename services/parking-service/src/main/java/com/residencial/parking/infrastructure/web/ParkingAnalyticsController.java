package com.residencial.parking.infrastructure.web;

import com.residencial.parking.application.dto.ApplyParkingFineRequest;
import com.residencial.parking.application.dto.CovertResidentAlertResponse;
import com.residencial.parking.application.dto.ParkingFineResponse;
import com.residencial.parking.application.usecase.ApplyParkingFineUseCase;
import com.residencial.parking.application.usecase.DetectCovertResidentsUseCase;
import com.residencial.parking.domain.repository.ParkingFineRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parking")
public class ParkingAnalyticsController {

    private final DetectCovertResidentsUseCase detectCovertResidentsUseCase;
    private final ApplyParkingFineUseCase applyParkingFineUseCase;
    private final ParkingFineRepository fineRepository;

    public ParkingAnalyticsController(DetectCovertResidentsUseCase detectCovertResidentsUseCase,
                                     ApplyParkingFineUseCase applyParkingFineUseCase,
                                     ParkingFineRepository fineRepository) {
        this.detectCovertResidentsUseCase = detectCovertResidentsUseCase;
        this.applyParkingFineUseCase = applyParkingFineUseCase;
        this.fineRepository = fineRepository;
    }

    @GetMapping("/analytics/covert-residents")
    public ResponseEntity<List<CovertResidentAlertResponse>> getCovertResidentSuspects(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        List<CovertResidentAlertResponse> suspects = detectCovertResidentsUseCase.execute(days);
        return ResponseEntity.ok(suspects);
    }

    @PostMapping("/fines")
    public ResponseEntity<ParkingFineResponse> applyFine(@Valid @RequestBody ApplyParkingFineRequest request) {
        ParkingFineResponse response = applyParkingFineUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/fines")
    public ResponseEntity<List<ParkingFineResponse>> listFines(
            @RequestParam(required = false) String plate) {
        List<ParkingFineResponse> fines;
        if (plate != null && !plate.isBlank()) {
            fines = fineRepository.findByPlate(plate).stream()
                    .map(ParkingFineResponse::fromDomain)
                    .toList();
        } else {
            fines = fineRepository.findAll().stream()
                    .map(ParkingFineResponse::fromDomain)
                    .toList();
        }
        return ResponseEntity.ok(fines);
    }
}
