package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.AmenityResponse;
import com.residencial.amenities.application.dto.CreateAmenityRequest;
import com.residencial.amenities.domain.exception.DuplicateAmenityCodeException;
import com.residencial.amenities.domain.model.Amenity;
import com.residencial.amenities.domain.repository.AmenityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigureAmenityUseCase {

    private final AmenityRepository amenityRepository;

    public ConfigureAmenityUseCase(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    @Transactional
    public AmenityResponse execute(CreateAmenityRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Amenity request cannot be null.");
        }

        String normalizedCode = request.code().trim().toUpperCase();
        if (amenityRepository.existsByCode(normalizedCode)) {
            throw new DuplicateAmenityCodeException(normalizedCode);
        }

        Amenity amenity = new Amenity(
                normalizedCode,
                request.name(),
                request.description(),
                request.capacity(),
                request.rentalCost(),
                request.depositCost(),
                request.openingTime(),
                request.closingTime(),
                request.allowedEventTypes()
        );

        Amenity saved = amenityRepository.save(amenity);

        return new AmenityResponse(
                saved.getId(),
                saved.getCode(),
                saved.getName(),
                saved.getDescription(),
                saved.getCapacity(),
                saved.getRentalCost(),
                saved.getDepositCost(),
                saved.getOpeningTime(),
                saved.getClosingTime(),
                saved.getAllowedEventTypes(),
                saved.isActive(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
