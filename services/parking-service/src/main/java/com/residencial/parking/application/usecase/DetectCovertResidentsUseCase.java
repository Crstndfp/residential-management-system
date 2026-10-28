package com.residencial.parking.application.usecase;

import com.residencial.parking.application.dto.CovertResidentAlertResponse;
import com.residencial.parking.domain.model.CovertResidentSuspect;
import com.residencial.parking.domain.model.VehicleEntry;
import com.residencial.parking.domain.repository.VehicleEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DetectCovertResidentsUseCase {

    private final VehicleEntryRepository entryRepository;

    public DetectCovertResidentsUseCase(VehicleEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Transactional(readOnly = true)
    public List<CovertResidentAlertResponse> execute(Integer days) {
        int windowDays = (days != null && days > 0) ? days : 7;
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(windowDays, ChronoUnit.DAYS);

        List<VehicleEntry> overnightEntries = entryRepository.findOvernightEntriesInWindow(startTime, endTime);

        Map<String, List<VehicleEntry>> entriesByPlate = overnightEntries.stream()
                .collect(Collectors.groupingBy(VehicleEntry::getPlate));

        List<CovertResidentAlertResponse> alerts = new ArrayList<>();

        for (Map.Entry<String, List<VehicleEntry>> mapEntry : entriesByPlate.entrySet()) {
            String plate = mapEntry.getKey();
            List<VehicleEntry> entries = mapEntry.getValue();

            UUID houseId = entries.getFirst().getHouseId();
            String houseNumber = entries.getFirst().getHouseNumber();

            CovertResidentSuspect suspect = CovertResidentSuspect.fromEntries(plate, houseId, houseNumber, entries);
            alerts.add(CovertResidentAlertResponse.fromDomain(suspect));
        }

        alerts.sort(Comparator.comparing(CovertResidentAlertResponse::overnightCount).reversed());
        return alerts;
    }
}
