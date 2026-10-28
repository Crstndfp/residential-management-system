package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.AmenityResponse;
import com.residencial.amenities.domain.model.Amenity;
import com.residencial.amenities.domain.repository.AmenityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListAmenitiesUseCase {

    private final AmenityRepository amenityRepository;

    public ListAmenitiesUseCase(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    @Transactional(readOnly = true)
    public List<AmenityResponse> execute(boolean activeOnly) {
        List<Amenity> list = activeOnly ? amenityRepository.findAllActive() : amenityRepository.findAll();
        return list.stream()
                .map(a -> new AmenityResponse(
                        a.getId(),
                        a.getCode(),
                        a.getName(),
                        a.getDescription(),
                        a.getCapacity(),
                        a.getRentalCost(),
                        a.getDepositCost(),
                        a.getOpeningTime(),
                        a.getClosingTime(),
                        a.getAllowedEventTypes(),
                        a.isActive(),
                        a.getCreatedAt(),
                        a.getUpdatedAt()
                ))
                .toList();
    }
}
