package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.MotionResponse;
import com.residencial.amenities.domain.exception.MotionNotFoundException;
import com.residencial.amenities.domain.model.AssemblyMotion;
import com.residencial.amenities.domain.repository.AssemblyMotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OpenMotionUseCase {

    private final AssemblyMotionRepository motionRepository;

    public OpenMotionUseCase(AssemblyMotionRepository motionRepository) {
        this.motionRepository = motionRepository;
    }

    @Transactional
    public MotionResponse execute(UUID motionId) {
        AssemblyMotion motion = motionRepository.findById(motionId)
                .orElseThrow(() -> new MotionNotFoundException(motionId));

        motion.open();
        AssemblyMotion saved = motionRepository.save(motion);
        return MotionResponse.fromDomain(saved);
    }
}
