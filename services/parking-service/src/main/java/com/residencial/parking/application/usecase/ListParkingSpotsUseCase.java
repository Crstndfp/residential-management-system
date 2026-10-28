package com.residencial.parking.application.usecase;

import com.residencial.parking.application.dto.ParkingSpotStatusResponse;
import com.residencial.parking.domain.repository.ParkingSpotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListParkingSpotsUseCase {

    private final ParkingSpotRepository spotRepository;

    public ListParkingSpotsUseCase(ParkingSpotRepository spotRepository) {
        this.spotRepository = spotRepository;
    }

    @Transactional(readOnly = true)
    public List<ParkingSpotStatusResponse> execute() {
        return spotRepository.findAllOrderBySpotNumber().stream()
                .map(ParkingSpotStatusResponse::fromDomain)
                .toList();
    }
}
