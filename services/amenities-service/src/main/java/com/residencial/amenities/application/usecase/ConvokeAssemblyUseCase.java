package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.AssemblyResponse;
import com.residencial.amenities.application.dto.ConvokeAssemblyRequest;
import com.residencial.amenities.domain.model.Assembly;
import com.residencial.amenities.domain.model.AssemblyStatus;
import com.residencial.amenities.domain.model.AssemblyType;
import com.residencial.amenities.domain.repository.AssemblyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ConvokeAssemblyUseCase {

    private final AssemblyRepository assemblyRepository;

    public ConvokeAssemblyUseCase(AssemblyRepository assemblyRepository) {
        this.assemblyRepository = assemblyRepository;
    }

    @Transactional
    public AssemblyResponse execute(ConvokeAssemblyRequest request) {
        AssemblyType type = AssemblyType.valueOf(request.assemblyType().toUpperCase().trim());
        int totalHouses = (request.totalResidentialHouses() != null && request.totalResidentialHouses() > 0)
                ? request.totalResidentialHouses()
                : 100;

        Assembly assembly = new Assembly(
                UUID.randomUUID(),
                request.title(),
                type,
                request.scheduledAt(),
                request.agenda(),
                AssemblyStatus.CONVOKED,
                totalHouses,
                Instant.now(),
                Instant.now()
        );

        Assembly saved = assemblyRepository.save(assembly);
        return AssemblyResponse.fromDomain(saved);
    }
}
