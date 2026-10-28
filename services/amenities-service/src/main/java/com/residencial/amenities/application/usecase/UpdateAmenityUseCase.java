package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.AmenityResponse;
import com.residencial.amenities.application.dto.UpdateAmenityRequest;
import com.residencial.amenities.domain.exception.AmenityNotFoundException;
import com.residencial.amenities.domain.model.Amenity;
import com.residencial.amenities.domain.repository.AmenityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateAmenityUseCase {

    private final AmenityRepository amenityRepository;

    public UpdateAmenityUseCase(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    @Transactional
    public AmenityResponse execute(UUID id, UpdateAmenityRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("Amenity ID cannot be null.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Update request cannot be null.");
        }

        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException("Amenity with ID '" + id + "' was not found."));

        amenity.update(
                request.name(),
                request.description(),
                request.capacity() != null ? request.capacity() : amenity.getCapacity(),
                request.rentalCost(),
                request.depositCost(),
                request.openingTime(),
                request.closingTime(),
                request.allowedEventTypes()
        );

        Amenity updated = amenityRepository.save(amenity);

        return new AmenityResponse(
                updated.getId(),
                updated.getCode(),
                updated.getName(),
                updated.getDescription(),
                updated.getCapacity(),
                updated.getRentalCost(),
                updated.getDepositCost(),
                updated.getOpeningTime(),
                updated.getClosingTime(),
                updated.getAllowedEventTypes(),
                updated.isActive(),
                updated.getCreatedAt(),
                updated.getUpdatedAt()
        );
    }
}
