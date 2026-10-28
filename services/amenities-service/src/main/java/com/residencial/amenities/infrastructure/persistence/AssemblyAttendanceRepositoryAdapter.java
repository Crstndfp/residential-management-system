package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.AssemblyAttendance;
import com.residencial.amenities.domain.repository.AssemblyAttendanceRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AssemblyAttendanceRepositoryAdapter implements AssemblyAttendanceRepository {

    private final JpaAssemblyAttendanceRepository jpaAttendanceRepository;

    public AssemblyAttendanceRepositoryAdapter(JpaAssemblyAttendanceRepository jpaAttendanceRepository) {
        this.jpaAttendanceRepository = jpaAttendanceRepository;
    }

    @Override
    public AssemblyAttendance save(AssemblyAttendance attendance) {
        return jpaAttendanceRepository.save(attendance);
    }

    @Override
    public List<AssemblyAttendance> findByAssemblyId(UUID assemblyId) {
        return jpaAttendanceRepository.findByAssemblyId(assemblyId);
    }

    @Override
    public Optional<AssemblyAttendance> findByAssemblyIdAndHouseId(UUID assemblyId, UUID houseId) {
        return jpaAttendanceRepository.findByAssemblyIdAndHouseId(assemblyId, houseId);
    }

    @Override
    public long countByAssemblyIdAndIsPresentTrue(UUID assemblyId) {
        return jpaAttendanceRepository.countByAssemblyIdAndIsPresentTrue(assemblyId);
    }
}
