package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.AssemblyQuorumResponse;
import com.residencial.amenities.domain.exception.AssemblyNotFoundException;
import com.residencial.amenities.domain.model.Assembly;
import com.residencial.amenities.domain.repository.AssemblyAttendanceRepository;
import com.residencial.amenities.domain.repository.AssemblyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class GetAssemblyQuorumUseCase {

    private final AssemblyRepository assemblyRepository;
    private final AssemblyAttendanceRepository attendanceRepository;

    public GetAssemblyQuorumUseCase(AssemblyRepository assemblyRepository,
                                    AssemblyAttendanceRepository attendanceRepository) {
        this.assemblyRepository = assemblyRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public AssemblyQuorumResponse execute(UUID assemblyId) {
        Assembly assembly = assemblyRepository.findById(assemblyId)
                .orElseThrow(() -> new AssemblyNotFoundException(assemblyId));

        long presentHouses = attendanceRepository.countByAssemblyIdAndIsPresentTrue(assemblyId);
        int totalHouses = assembly.getTotalResidentialHouses();

        double rawPercentage = (presentHouses * 100.0) / totalHouses;
        double quorumPercentage = BigDecimal.valueOf(rawPercentage)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        boolean hasQuorum = quorumPercentage >= 50.0;

        return new AssemblyQuorumResponse(
                assembly.getId(),
                assembly.getTitle(),
                assembly.getStatus().name(),
                totalHouses,
                presentHouses,
                quorumPercentage,
                hasQuorum
        );
    }
}
