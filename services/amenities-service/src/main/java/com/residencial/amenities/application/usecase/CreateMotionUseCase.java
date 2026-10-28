package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.CreateMotionRequest;
import com.residencial.amenities.application.dto.MotionResponse;
import com.residencial.amenities.domain.exception.AssemblyNotFoundException;
import com.residencial.amenities.domain.model.AssemblyMotion;
import com.residencial.amenities.domain.model.MotionStatus;
import com.residencial.amenities.domain.repository.AssemblyMotionRepository;
import com.residencial.amenities.domain.repository.AssemblyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateMotionUseCase {

    private final AssemblyRepository assemblyRepository;
    private final AssemblyMotionRepository motionRepository;

    public CreateMotionUseCase(AssemblyRepository assemblyRepository,
                               AssemblyMotionRepository motionRepository) {
        this.assemblyRepository = assemblyRepository;
        this.motionRepository = motionRepository;
    }

    @Transactional
    public MotionResponse execute(UUID assemblyId, CreateMotionRequest request) {
        assemblyRepository.findById(assemblyId)
                .orElseThrow(() -> new AssemblyNotFoundException(assemblyId));

        AssemblyMotion motion = new AssemblyMotion(
                UUID.randomUUID(),
                assemblyId,
                request.title(),
                request.description(),
                MotionStatus.PENDING,
                null,
                null,
                null,
                Instant.now()
        );

        AssemblyMotion saved = motionRepository.save(motion);
        return MotionResponse.fromDomain(saved);
    }
}
