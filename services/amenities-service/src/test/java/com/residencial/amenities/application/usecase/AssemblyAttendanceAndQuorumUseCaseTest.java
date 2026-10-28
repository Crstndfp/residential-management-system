package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.AssemblyQuorumResponse;
import com.residencial.amenities.application.dto.AttendanceResponse;
import com.residencial.amenities.application.dto.RegisterAttendanceRequest;
import com.residencial.amenities.domain.exception.AssemblyNotFoundException;
import com.residencial.amenities.domain.model.Assembly;
import com.residencial.amenities.domain.model.AssemblyAttendance;
import com.residencial.amenities.domain.model.AssemblyStatus;
import com.residencial.amenities.domain.model.AssemblyType;
import com.residencial.amenities.domain.repository.AssemblyAttendanceRepository;
import com.residencial.amenities.domain.repository.AssemblyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssemblyAttendanceAndQuorumUseCaseTest {

    private AssemblyRepository assemblyRepository;
    private AssemblyAttendanceRepository attendanceRepository;
    private RegisterAttendanceUseCase registerAttendanceUseCase;
    private GetAssemblyQuorumUseCase getAssemblyQuorumUseCase;

    @BeforeEach
    void setUp() {
        assemblyRepository = mock(AssemblyRepository.class);
        attendanceRepository = mock(AssemblyAttendanceRepository.class);
        registerAttendanceUseCase = new RegisterAttendanceUseCase(assemblyRepository, attendanceRepository);
        getAssemblyQuorumUseCase = new GetAssemblyQuorumUseCase(assemblyRepository, attendanceRepository);
    }

    @Test
    @DisplayName("Successfully registers attendance for a house during digital roll call")
    void registerAttendanceSuccess() {
        UUID assemblyId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Assembly assembly = new Assembly(
                assemblyId,
                "Assembly 2026",
                AssemblyType.ORDINARY,
                Instant.now(),
                "Agenda",
                AssemblyStatus.CONVOKED,
                100,
                Instant.now(),
                Instant.now()
        );

        when(assemblyRepository.findById(assemblyId)).thenReturn(Optional.of(assembly));
        when(attendanceRepository.findByAssemblyIdAndHouseId(assemblyId, houseId)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AssemblyAttendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterAttendanceRequest request = new RegisterAttendanceRequest(
                houseId,
                "A-101",
                userId,
                "John Doe",
                true
        );

        AttendanceResponse response = registerAttendanceUseCase.execute(assemblyId, request);

        assertThat(response).isNotNull();
        assertThat(response.assemblyId()).isEqualTo(assemblyId);
        assertThat(response.houseId()).isEqualTo(houseId);
        assertThat(response.houseNumber()).isEqualTo("A-101");
        assertThat(response.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Calculating quorum: hasQuorum is false when attendance < 50%")
    void quorumUnderFiftyPercent() {
        UUID assemblyId = UUID.randomUUID();
        Assembly assembly = new Assembly(
                assemblyId,
                "Assembly 2026",
                AssemblyType.ORDINARY,
                Instant.now(),
                "Agenda",
                AssemblyStatus.CONVOKED,
                100,
                Instant.now(),
                Instant.now()
        );

        when(assemblyRepository.findById(assemblyId)).thenReturn(Optional.of(assembly));
        when(attendanceRepository.countByAssemblyIdAndIsPresentTrue(assemblyId)).thenReturn(45L);

        AssemblyQuorumResponse response = getAssemblyQuorumUseCase.execute(assemblyId);

        assertThat(response.totalResidentialHouses()).isEqualTo(100);
        assertThat(response.presentHouses()).isEqualTo(45L);
        assertThat(response.quorumPercentage()).isEqualTo(45.0);
        assertThat(response.hasQuorum()).isFalse();
    }

    @Test
    @DisplayName("Calculating quorum: hasQuorum is true when attendance >= 50%")
    void quorumFiftyPercentOrMore() {
        UUID assemblyId = UUID.randomUUID();
        Assembly assembly = new Assembly(
                assemblyId,
                "Assembly 2026",
                AssemblyType.ORDINARY,
                Instant.now(),
                "Agenda",
                AssemblyStatus.IN_PROGRESS,
                80,
                Instant.now(),
                Instant.now()
        );

        when(assemblyRepository.findById(assemblyId)).thenReturn(Optional.of(assembly));
        when(attendanceRepository.countByAssemblyIdAndIsPresentTrue(assemblyId)).thenReturn(48L);

        AssemblyQuorumResponse response = getAssemblyQuorumUseCase.execute(assemblyId);

        // 48 / 80 * 100 = 60.0%
        assertThat(response.totalResidentialHouses()).isEqualTo(80);
        assertThat(response.presentHouses()).isEqualTo(48L);
        assertThat(response.quorumPercentage()).isEqualTo(60.0);
        assertThat(response.hasQuorum()).isTrue();
    }

    @Test
    @DisplayName("Throws AssemblyNotFoundException when querying quorum for non-existent assembly")
    void quorumAssemblyNotFound() {
        UUID assemblyId = UUID.randomUUID();
        when(assemblyRepository.findById(assemblyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getAssemblyQuorumUseCase.execute(assemblyId))
                .isInstanceOf(AssemblyNotFoundException.class);
    }
}
