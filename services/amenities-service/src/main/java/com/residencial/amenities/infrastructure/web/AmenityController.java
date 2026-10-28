package com.residencial.amenities.infrastructure.web;

import com.residencial.amenities.application.dto.AmenityResponse;
import com.residencial.amenities.application.dto.CalendarSlotDto;
import com.residencial.amenities.application.dto.CreateAmenityRequest;
import com.residencial.amenities.application.dto.UpdateAmenityRequest;
import com.residencial.amenities.application.usecase.ConfigureAmenityUseCase;
import com.residencial.amenities.application.usecase.GetAmenityCalendarUseCase;
import com.residencial.amenities.application.usecase.ListAmenitiesUseCase;
import com.residencial.amenities.application.usecase.UpdateAmenityUseCase;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/amenities")
public class AmenityController {

    private final ConfigureAmenityUseCase configureAmenityUseCase;
    private final UpdateAmenityUseCase updateAmenityUseCase;
    private final ListAmenitiesUseCase listAmenitiesUseCase;
    private final GetAmenityCalendarUseCase getAmenityCalendarUseCase;

    public AmenityController(ConfigureAmenityUseCase configureAmenityUseCase,
                             UpdateAmenityUseCase updateAmenityUseCase,
                             ListAmenitiesUseCase listAmenitiesUseCase,
                             GetAmenityCalendarUseCase getAmenityCalendarUseCase) {
        this.configureAmenityUseCase = configureAmenityUseCase;
        this.updateAmenityUseCase = updateAmenityUseCase;
        this.listAmenitiesUseCase = listAmenitiesUseCase;
        this.getAmenityCalendarUseCase = getAmenityCalendarUseCase;
    }

    @PostMapping
    public ResponseEntity<AmenityResponse> createAmenity(@Valid @RequestBody CreateAmenityRequest request) {
        AmenityResponse response = configureAmenityUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AmenityResponse> updateAmenity(@PathVariable UUID id,
                                                         @RequestBody UpdateAmenityRequest request) {
        AmenityResponse response = updateAmenityUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AmenityResponse>> listAmenities(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<AmenityResponse> list = listAmenitiesUseCase.execute(activeOnly);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/calendar")
    public ResponseEntity<List<CalendarSlotDto>> getAmenityCalendar(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CalendarSlotDto> calendar = getAmenityCalendarUseCase.execute(id, startDate, endDate);
        return ResponseEntity.ok(calendar);
    }
}
