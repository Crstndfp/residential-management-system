package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.AssemblyResponse;
import com.residencial.amenities.application.dto.ConvokeAssemblyRequest;
import com.residencial.amenities.domain.model.Assembly;
import com.residencial.amenities.domain.repository.AssemblyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConvokeAssemblyUseCaseTest {

    private AssemblyRepository assemblyRepository;
    private ConvokeAssemblyUseCase convokeAssemblyUseCase;

    @BeforeEach
    void setUp() {
        assemblyRepository = mock(AssemblyRepository.class);
        convokeAssemblyUseCase = new ConvokeAssemblyUseCase(assemblyRepository);
    }

    @Test
    @DisplayName("Convokes assembly with default 100 residential houses when not specified")
    void convokeAssemblyDefaultHouses() {
        ConvokeAssemblyRequest request = new ConvokeAssemblyRequest(
                "Ordinary General Assembly 2026",
                "ORDINARY",
                Instant.now().plusSeconds(86400),
                "Agenda: Financial review and board election",
                null
        );

        when(assemblyRepository.save(any(Assembly.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssemblyResponse response = convokeAssemblyUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Ordinary General Assembly 2026");
        assertThat(response.assemblyType()).isEqualTo("ORDINARY");
        assertThat(response.totalResidentialHouses()).isEqualTo(100);
        assertThat(response.status()).isEqualTo("CONVOKED");

        ArgumentCaptor<Assembly> captor = ArgumentCaptor.forClass(Assembly.class);
        verify(assemblyRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalResidentialHouses()).isEqualTo(100);
    }

    @Test
    @DisplayName("Convokes assembly with custom residential house count")
    void convokeAssemblyCustomHouses() {
        ConvokeAssemblyRequest request = new ConvokeAssemblyRequest(
                "Extraordinary Security Assembly",
                "EXTRAORDINARY",
                Instant.now().plusSeconds(43200),
                "Agenda: Surveillance cameras installation",
                150
        );

        when(assemblyRepository.save(any(Assembly.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssemblyResponse response = convokeAssemblyUseCase.execute(request);

        assertThat(response.totalResidentialHouses()).isEqualTo(150);
        assertThat(response.assemblyType()).isEqualTo("EXTRAORDINARY");
    }
}
