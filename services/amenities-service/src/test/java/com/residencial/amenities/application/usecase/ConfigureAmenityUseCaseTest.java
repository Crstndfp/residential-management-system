package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.AmenityResponse;
import com.residencial.amenities.application.dto.CreateAmenityRequest;
import com.residencial.amenities.domain.exception.DuplicateAmenityCodeException;
import com.residencial.amenities.domain.model.Amenity;
import com.residencial.amenities.domain.repository.AmenityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigureAmenityUseCaseTest {

    @Mock
    private AmenityRepository amenityRepository;

    private ConfigureAmenityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConfigureAmenityUseCase(amenityRepository);
    }

    @Test
    @DisplayName("US-301: Successfully configure new amenity")
    void shouldConfigureAmenitySuccessfully() {
        CreateAmenityRequest request = new CreateAmenityRequest(
                "PENNIS_COURT",
                "Cancha de Tenis",
                "Cancha de arcilla reglamentaria",
                4,
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                LocalTime.of(6, 0),
                LocalTime.of(21, 0),
                "SPORTS_MATCH,TRAINING"
        );

        when(amenityRepository.existsByCode("PENNIS_COURT")).thenReturn(false);
        when(amenityRepository.save(any(Amenity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AmenityResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals("PENNIS_COURT", response.code());
        assertEquals("Cancha de Tenis", response.name());
        assertEquals(4, response.capacity());
        assertEquals(new BigDecimal("100.00"), response.rentalCost());
        assertTrue(response.isActive());
        verify(amenityRepository).save(any(Amenity.class));
    }

    @Test
    @DisplayName("US-301 Invariant: Reject duplicate amenity code")
    void shouldRejectDuplicateCode() {
        CreateAmenityRequest request = new CreateAmenityRequest(
                "club_house",
                "Casa Club Duplicada",
                null,
                100,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                "PARTY"
        );

        when(amenityRepository.existsByCode("CLUB_HOUSE")).thenReturn(true);

        assertThrows(DuplicateAmenityCodeException.class, () -> useCase.execute(request));
        verify(amenityRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-301 Invariant: Reject null request")
    void shouldRejectNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }
}
